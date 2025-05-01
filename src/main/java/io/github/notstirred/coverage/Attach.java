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
    private static final String SORTABLE_JS = "var stIsIE=!1;\n" +
        "/*@cc_on @*/\n" +
        "if(sorttable={init:function(){arguments.callee.done||(arguments.callee.done=!0,_timer&&clearInterval(_timer),document.createElement&&document.getElementsByTagName&&(sorttable.DATE_RE=/^(\\d\\d?)[\\/\\.-](\\d\\d?)[\\/\\.-]((\\d\\d)?\\d\\d)$/,forEach(document.getElementsByTagName(\"table\"),(function(t){-1!=t.className.search(/\\bsortable\\b/)&&sorttable.makeSortable(t)}))))},makeSortable:function(t){if(0==t.getElementsByTagName(\"thead\").length&&(the=document.createElement(\"thead\"),the.appendChild(t.rows[0]),t.insertBefore(the,t.firstChild)),null==t.tHead&&(t.tHead=t.getElementsByTagName(\"thead\")[0]),1==t.tHead.rows.length){sortbottomrows=[];for(var e=0;e<t.rows.length;e++)-1!=t.rows[e].className.search(/\\bsortbottom\\b/)&&(sortbottomrows[sortbottomrows.length]=t.rows[e]);if(sortbottomrows){null==t.tFoot&&(tfo=document.createElement(\"tfoot\"),t.appendChild(tfo));for(e=0;e<sortbottomrows.length;e++)tfo.appendChild(sortbottomrows[e]);delete sortbottomrows}headrow=t.tHead.rows[0].cells;for(e=0;e<headrow.length;e++)headrow[e].className.match(/\\bsorttable_nosort\\b/)||(mtch=headrow[e].className.match(/\\bsorttable_([a-z0-9]+)\\b/),mtch&&(override=mtch[1]),mtch&&\"function\"==typeof sorttable[\"sort_\"+override]?headrow[e].sorttable_sortfunction=sorttable[\"sort_\"+override]:headrow[e].sorttable_sortfunction=sorttable.guessType(t,e),headrow[e].sorttable_columnindex=e,headrow[e].sorttable_tbody=t.tBodies[0],dean_addEvent(headrow[e],\"click\",sorttable.innerSortFunction=function(t){if(-1!=this.className.search(/\\bsorttable_sorted\\b/))return sorttable.reverse(this.sorttable_tbody),this.className=this.className.replace(\"sorttable_sorted\",\"sorttable_sorted_reverse\"),this.removeChild(document.getElementById(\"sorttable_sortfwdind\")),sortrevind=document.createElement(\"span\"),sortrevind.id=\"sorttable_sortrevind\",sortrevind.innerHTML=stIsIE?'&nbsp<font face=\"webdings\">5</font>':\"&nbsp;&#x25B4;\",void this.appendChild(sortrevind);if(-1!=this.className.search(/\\bsorttable_sorted_reverse\\b/))return sorttable.reverse(this.sorttable_tbody),this.className=this.className.replace(\"sorttable_sorted_reverse\",\"sorttable_sorted\"),this.removeChild(document.getElementById(\"sorttable_sortrevind\")),sortfwdind=document.createElement(\"span\"),sortfwdind.id=\"sorttable_sortfwdind\",sortfwdind.innerHTML=stIsIE?'&nbsp<font face=\"webdings\">6</font>':\"&nbsp;&#x25BE;\",void this.appendChild(sortfwdind);theadrow=this.parentNode,forEach(theadrow.childNodes,(function(t){1==t.nodeType&&(t.className=t.className.replace(\"sorttable_sorted_reverse\",\"\"),t.className=t.className.replace(\"sorttable_sorted\",\"\"))})),sortfwdind=document.getElementById(\"sorttable_sortfwdind\"),sortfwdind&&sortfwdind.parentNode.removeChild(sortfwdind),sortrevind=document.getElementById(\"sorttable_sortrevind\"),sortrevind&&sortrevind.parentNode.removeChild(sortrevind),this.className+=\" sorttable_sorted\",sortfwdind=document.createElement(\"span\"),sortfwdind.id=\"sorttable_sortfwdind\",sortfwdind.innerHTML=stIsIE?'&nbsp<font face=\"webdings\">6</font>':\"&nbsp;&#x25BE;\",this.appendChild(sortfwdind),row_array=[],col=this.sorttable_columnindex,rows=this.sorttable_tbody.rows;for(var e=0;e<rows.length;e++)row_array[row_array.length]=[sorttable.getInnerText(rows[e].cells[col]),rows[e]];row_array.sort(this.sorttable_sortfunction),tb=this.sorttable_tbody;for(e=0;e<row_array.length;e++)tb.appendChild(row_array[e][1]);delete row_array}))}},guessType:function(t,e){sortfn=sorttable.sort_alpha;for(var r=0;r<t.tBodies[0].rows.length;r++)if(text=sorttable.getInnerText(t.tBodies[0].rows[r].cells[e]),\"\"!=text){if(text.match(/^-?[£$¤]?[\\d,.]+%?$/))return sorttable.sort_numeric;if(possdate=text.match(sorttable.DATE_RE),possdate){if(first=parseInt(possdate[1]),second=parseInt(possdate[2]),first>12)return sorttable.sort_ddmm;if(second>12)return sorttable.sort_mmdd;sortfn=sorttable.sort_ddmm}}return sortfn},getInnerText:function(t){if(!t)return\"\";if(hasInputs=\"function\"==typeof t.getElementsByTagName&&t.getElementsByTagName(\"input\").length,null!=t.getAttribute(\"sorttable_customkey\"))return t.getAttribute(\"sorttable_customkey\");if(void 0!==t.textContent&&!hasInputs)return t.textContent.replace(/^\\s+|\\s+$/g,\"\");if(void 0!==t.innerText&&!hasInputs)return t.innerText.replace(/^\\s+|\\s+$/g,\"\");if(void 0!==t.text&&!hasInputs)return t.text.replace(/^\\s+|\\s+$/g,\"\");switch(t.nodeType){case 3:if(\"input\"==t.nodeName.toLowerCase())return t.value.replace(/^\\s+|\\s+$/g,\"\");case 4:return t.nodeValue.replace(/^\\s+|\\s+$/g,\"\");case 1:case 11:for(var e=\"\",r=0;r<t.childNodes.length;r++)e+=sorttable.getInnerText(t.childNodes[r]);return e.replace(/^\\s+|\\s+$/g,\"\");default:return\"\"}},reverse:function(t){newrows=[];for(var e=0;e<t.rows.length;e++)newrows[newrows.length]=t.rows[e];for(e=newrows.length-1;e>=0;e--)t.appendChild(newrows[e]);delete newrows},sort_numeric:function(t,e){return aa=parseFloat(t[0].replace(/[^0-9.-]/g,\"\")),isNaN(aa)&&(aa=0),bb=parseFloat(e[0].replace(/[^0-9.-]/g,\"\")),isNaN(bb)&&(bb=0),aa-bb},sort_alpha:function(t,e){return t[0]==e[0]?0:t[0]<e[0]?-1:1},sort_ddmm:function(t,e){return mtch=t[0].match(sorttable.DATE_RE),y=mtch[3],m=mtch[2],d=mtch[1],1==m.length&&(m=\"0\"+m),1==d.length&&(d=\"0\"+d),dt1=y+m+d,mtch=e[0].match(sorttable.DATE_RE),y=mtch[3],m=mtch[2],d=mtch[1],1==m.length&&(m=\"0\"+m),1==d.length&&(d=\"0\"+d),dt2=y+m+d,dt1==dt2?0:dt1<dt2?-1:1},sort_mmdd:function(t,e){return mtch=t[0].match(sorttable.DATE_RE),y=mtch[3],d=mtch[2],m=mtch[1],1==m.length&&(m=\"0\"+m),1==d.length&&(d=\"0\"+d),dt1=y+m+d,mtch=e[0].match(sorttable.DATE_RE),y=mtch[3],d=mtch[2],m=mtch[1],1==m.length&&(m=\"0\"+m),1==d.length&&(d=\"0\"+d),dt2=y+m+d,dt1==dt2?0:dt1<dt2?-1:1},shaker_sort:function(t,e){for(var r=0,o=t.length-1,n=!0;n;){n=!1;for(var s=r;s<o;++s)if(e(t[s],t[s+1])>0){var a=t[s];t[s]=t[s+1],t[s+1]=a,n=!0}if(o--,!n)break;for(s=o;s>r;--s)if(e(t[s],t[s-1])<0){a=t[s];t[s]=t[s-1],t[s-1]=a,n=!0}r++}}},document.addEventListener&&document.addEventListener(\"DOMContentLoaded\",sorttable.init,!1),/WebKit/i.test(navigator.userAgent))var _timer=setInterval((function(){/loaded|complete/.test(document.readyState)&&sorttable.init()}),10);function dean_addEvent(t,e,r){if(t.addEventListener)t.addEventListener(e,r,!1);else{r.$$guid||(r.$$guid=dean_addEvent.guid++),t.events||(t.events={});var o=t.events[e];o||(o=t.events[e]={},t[\"on\"+e]&&(o[0]=t[\"on\"+e])),o[r.$$guid]=r,t[\"on\"+e]=handleEvent}}function removeEvent(t,e,r){t.removeEventListener?t.removeEventListener(e,r,!1):t.events&&t.events[e]&&delete t.events[e][r.$$guid]}function handleEvent(t){var e=!0;t=t||fixEvent(((this.ownerDocument||this.document||this).parentWindow||window).event);var r=this.events[t.type];for(var o in r)this.$$handleEvent=r[o],!1===this.$$handleEvent(t)&&(e=!1);return e}function fixEvent(t){return t.preventDefault=fixEvent.preventDefault,t.stopPropagation=fixEvent.stopPropagation,t}window.onload=sorttable.init,dean_addEvent.guid=1,fixEvent.preventDefault=function(){this.returnValue=!1},fixEvent.stopPropagation=function(){this.cancelBubble=!0},Array.forEach||(Array.forEach=function(t,e,r){for(var o=0;o<t.length;o++)e.call(r,t[o],o,t)}),Function.prototype.forEach=function(t,e,r){for(var o in t)void 0===this.prototype[o]&&e.call(r,t[o],o,t)},String.forEach=function(t,e,r){Array.forEach(t.split(\"\"),(function(o,n){e.call(r,o,n,t)}))};var forEach=function(t,e,r){if(t){var o=Object;if(t instanceof Function)o=Function;else{if(t.forEach instanceof Function)return void t.forEach(e,r);\"string\"==typeof t?o=String:\"number\"==typeof t.length&&(o=Array)}o.forEach(t,e,r)}};";


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
                        methodData.lines.forEach(line -> {
                            if (line.idx < 0) { // this line isn't tracked by coverage (line numbers, labels, etc.)
                                elements.add(addLabelLinks(methodSig, line));
                            } else { // tracked by coverage
                                if (data[line.idx] == 1) { // coverage hit
                                    elements.add(addLabelLinks(methodSig, line).withStyle("background-color: #AADD77;"));
                                } else { // coverage miss
                                    elements.add(addLabelLinks(methodSig, line).withStyle("background-color: #d99;"));
                                }
                            }
                        });
                        float coverage = calculateCoverageForRange(indices, data);
                        methodTotal[0] += coverage;
                        methodCount[0]++;
                        body.with(
                            div(
                                details(
                                    summary(methodSig + " " + coverage + "%")
                                        .withStyle("background-color: ccc; padding: 10px; border-radius: 2px;"),
                                    pre(elements.toArray(new DomContent[0]))
                                        .withStyle("margin: 0px; padding-bottom: 10px; padding-top: 5px;")
                                ).withCondOpen(true)
                                    .withStyle("background-color: #f6f6f6; border-radius: 2px;")
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
            try {
                Files.write(path, SORTABLE_JS.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Failed to write file: " + path.toAbsolutePath());
                e.printStackTrace();
            }
        }));
    }

    private static DivTag addLabelLinks(String methodSig, CoverageAddingMethodVisitor.LineData line) {
        List<DomContent> elements = new ArrayList<>();

        if (!line.labelReferences.isEmpty()) {
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
            elements.add(text(line.line));
            elements.add(div()
                    .withId(methodSig + "_" + line.line.substring(line.labelDefinition.get().range.a, line.labelDefinition.get().range.b))
                    .withStyle("transform: translateY(-50vh);"));
        } else {
            elements.add(text(line.line));
        }
        return div(elements.toArray(new DomContent[0]));
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
            content.with(head(script()
                .withSrc(String.valueOf(directory.relativize(USER_DIR).resolve("sortable.js")))
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
