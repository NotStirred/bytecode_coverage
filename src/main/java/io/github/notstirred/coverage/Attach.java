package io.github.notstirred.coverage;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import j2html.tags.DomContent;
import j2html.tags.specialized.BodyTag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.HtmlTag;
import j2html.tags.specialized.TableTag;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static j2html.TagCreator.*;

public class Attach {
    public static final Object2ObjectMap<String, Object2ObjectMap<String, Object2ObjectMap<IntPair, MethodData>>> METHOD_INDICES = new Object2ObjectOpenHashMap<>();
    private static final Path USER_DIR = Paths.get(System.getProperty("user.dir")).resolve("build/bytecode_coverage");

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Bytecode coverage active!");
        inst.addTransformer(new Transformer());
    }
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("Bytecode coverage active!");
        inst.addTransformer(new Transformer());
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Wait for data to be transferred to the system classloader before continuing.
            try {
                int i = 0;
                while (!BytecodeCoverageData.getSharedWritten()) {
                    if (i > 100) {
                        System.out.println("Timed out while waiting for bytecode coverage data.");
                        return;
                    }
                    Thread.sleep(50);
                    i++;
                }
            } catch (InterruptedException e) {
                System.out.println("Bytecode coverage receiving interrupted!");
                return;
            }

            byte[] data = BytecodeCoverageData.getSharedData();

            TableTag indexTable = table(thead(tr(td("Package Name"), td("Coverage")))).withClass("sortable");
            METHOD_INDICES.forEach((packageName, methodsByClass) -> {

                TableTag packageTable = table(thead(tr(td("Class Name"), td("Coverage")))).withClass("sortable");

                double[] packageTotal = new double[] { 0.0 };
                int[] packageCount = new int[] { 0 };

                methodsByClass.forEach((className, methodIndices) -> {
                    double[] methodTotal = new double[] { 0.0 };
                    int[] methodCount = new int[] { 0 };

                    BodyTag body = body(h1(className));
                    methodIndices.forEach((indices, methodData) -> {
                        List<DomContent> elements = new ArrayList<>();
                        String methodSig = methodData.methodNode.name + methodData.methodNode.desc;
                        boolean even = false;  // Parity of the current line number, for alternating colors
                        int rowIndex = 1; // Index of the current row within the CSS grid
                        int startOfLineRowIndex = -1; // Index of the start of the current line number within the CSS grid
                        for (CoverageAddingMethodVisitor.LineData line : methodData.lines) {
                            boolean bumpRowIndex = false;
                            String cssClass;
                            if (line.line.contains("LINENUMBER")) {
                                if (startOfLineRowIndex != -1) { // end of a line; add background color
                                    elements.add(div().withClasses("line-number-and-label-background", even ? "even-line" : "odd-line")
                                            .withStyle("grid-row: " + startOfLineRowIndex + " / " + rowIndex)
                                    );
                                    even = !even;
                                }
                                startOfLineRowIndex = rowIndex;
                                cssClass = "line-number";
                            } else if (line.labelDefinition.isPresent()) {
                                cssClass = "label-define";
                            } else {
                                cssClass = "bytecode";
                                // not a line number or label definition; increment rowIndex
                                bumpRowIndex = true;
                            }
                            String cssCoverageClass;
                            if (line.idx < 0) { // this line isn't tracked by coverage (line numbers, labels, etc.)
                                cssCoverageClass = "coverage-ignored";
                            } else { // tracked by coverage
                                if (data[line.idx] == 1) { // coverage hit
                                    cssCoverageClass = "coverage-hit";
                                } else { // coverage miss
                                    cssCoverageClass = "coverage-miss";
                                }
                            }
                            elements.add(addLabelLinks(methodSig, line, rowIndex).withClasses(cssClass, even ? "even-line" : "odd-line", cssCoverageClass));
                            if (bumpRowIndex) rowIndex++;
                        }
                        // Add background color for final line
                        elements.add(div().withClasses("line-number-and-label-background", even ? "even-line" : "odd-line")
                                .withStyle("grid-row: " + startOfLineRowIndex + " / " + rowIndex)
                        );
                        float coverage = calculateCoverageForRange(indices, data);
                        methodTotal[0] += coverage;
                        methodCount[0]++;
                        body.with(
                            div(
                                details(
                                    summary(methodSig + " " + coverage + "%")
                                        .withClass("method-info-header"),
                                    pre(elements.toArray(new DomContent[0]))
                                        .withClass("bytecode-container")
                                ).withCondOpen(true)
                                    .withStyle("border-radius: 2px;")
                            ).withStyle("padding: 10px; padding-top: 0px;")
                        ).withStyle("background-color: #fff;");
                    });

                    packageCount[0] += methodCount[0];
                    packageTotal[0] += methodTotal[0];
                    packageTable.with(tr(td(a(className).withHref(className + ".html")), td(methodCount[0] == 0 ? "N/A" : (methodTotal[0] / methodCount[0] + "%"))));

                    writeFile(packageName + "/" + className + ".html", html(body));
                });

                indexTable.with(tr(td(a(packageName).withHref(packageName + "/index.html")), td(packageCount[0] == 0 ? "N/A" : (packageTotal[0] / packageCount[0] + "%"))));

                writeFile(packageName + "/index.html", html(body(packageTable)));
            });

            writeFile("index.html", html(body(indexTable)));
            Path path = USER_DIR.resolve("sortable.js");
            try (InputStream inputStream = Attach.class.getClassLoader().getResourceAsStream("sortable.js")) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Failed to write file: " + path.toAbsolutePath());
                e.printStackTrace();
            }
            Path path2 = USER_DIR.resolve("style.css");
            try (InputStream inputStream = Attach.class.getClassLoader().getResourceAsStream("style.css")) {
                Files.copy(inputStream, path2, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Failed to write file: " + path.toAbsolutePath());
                e.printStackTrace();
            }
        }));
    }

    private static DivTag addLabelLinks(String methodSig, CoverageAddingMethodVisitor.LineData line, int rowIndex) {
        List<DomContent> elements = new ArrayList<>();

        int lineNumberIdx = line.line.indexOf("LINENUMBER");
        if (lineNumberIdx > -1) { // For line numbers, only display the number itself
            String temp = line.line.substring(lineNumberIdx + "LINENUMBER ".length());
            temp = temp.substring(0, temp.indexOf(" "));
            elements.add(text(temp));
        } else if (!line.labelReferences.isEmpty()) {
            int idx = 0;
            for (CoverageAddingMethodVisitor.LabelData labelReference : line.labelReferences) {
                // add the string bit before this label
                if (idx < labelReference.range.a) {
                    elements.add(text(line.line.substring(idx, labelReference.range.a)));
                    idx = labelReference.range.a;
                }
                // add the label link and L# text
                String label = line.line.substring(idx, labelReference.range.b);
                elements.add(a(label).withHref("#" + methodSig + "_" + label));
                idx = labelReference.range.b;
            }
            // add the remaining text after the last label
            if (idx < line.line.length()) {
                elements.add(text(line.line.substring(idx)));
            }
        } else if (line.labelDefinition.isPresent()) {
            elements.add(text(line.line.trim()));
            elements.add(div()
                    .withId(methodSig + "_" + line.line.substring(line.labelDefinition.get().range.a, line.labelDefinition.get().range.b))
                    .withStyle("transform: translateY(-50vh);"));
        } else {
            elements.add(text(line.line));
        }
        return div(elements.toArray(new DomContent[0])).withStyle("grid-row: " + rowIndex);
    }

    private static float calculateCoverageForRange(IntPair range, byte[] data) {
        int size = range.b - range.a;

        if (size == 0) {
            return 100.0f;
        }

        int total = 0;
        for (int i = range.a; i < range.b; i++) {
            total += data[i];
        }
        return ((float) total / (float) size) * 100;
    }

    private static void writeFile(String path, HtmlTag content) {
        Path filePath = USER_DIR.resolve(path);
        Path directory = filePath.getParent();
        try {
            content.with(head(
                    script().withSrc(String.valueOf(directory.relativize(USER_DIR).resolve("sortable.js"))),
                    link().withRel("stylesheet").withHref(String.valueOf(directory.relativize(USER_DIR).resolve("style.css")))
            ));

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content.renderFormatted().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Failed to write file: " + filePath.toAbsolutePath());
            e.printStackTrace();
        }
    }

    public static class MethodData {
        public final MethodNode methodNode;
        public final List<CoverageAddingMethodVisitor.LineData> lines;

        public MethodData(MethodNode methodNode, List<CoverageAddingMethodVisitor.LineData> lines) {
            this.methodNode = methodNode;
            this.lines = lines;
        }
    }
}
