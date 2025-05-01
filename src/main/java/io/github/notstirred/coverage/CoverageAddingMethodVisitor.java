package io.github.notstirred.coverage;

import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.ASM9;

/**
 * This {@link MethodVisitor} is responsible for:
 * <ul>
 *   <li>Adding coverage tracking instructions via {@link #addCoverageByteCode()}</li>
 *   <li>Converting bytecode to a human-readable form while keeping coverage tracking metadata intact via {@link #addLineData(int)}</li>
 * </ul>
 *
 * <br/><br/>
 *
 * Each visit method must take care to call either:
 * <ul>
 *     <li>{@link #addCoverageByteCode()} if it adds coverage tracking</li>
 *     <li>{@link #addLineDataNoCoverage()} if it doesn't</li>
 * </ul>
 * If a single method fails to do so, {@link LabelRememberingTextifier} will be
 * out of sync and label links will be corrupted.
 */
public class CoverageAddingMethodVisitor extends MethodVisitor {
    public static final int NO_COVERAGE = -1;

    private final LabelRememberingTextifier printer;

    private final List<LineData> lines = new ArrayList<>();

    protected CoverageAddingMethodVisitor(MethodVisitor methodVisitor) {
        super(ASM9, methodVisitor);
        this.printer = new LabelRememberingTextifier(ASM9);
    }

    /**
     * Should be called <u>after</u> this method visitor has visited a single method.
     * @return The line data for the visited method.
     */
    public List<LineData> getLines() {
        return Collections.unmodifiableList(this.lines);
    }

    private void addCoverageByteCode() {
        addLineData(Transformer.idx);

        super.visitFieldInsn(Opcodes.GETSTATIC, "io/github/notstirred/coverage/BytecodeCoverageData", "DATA", "[B");
        super.visitLdcInsn(Transformer.idx++);
        super.visitInsn(Opcodes.ICONST_1);
        super.visitInsn(Opcodes.BASTORE);
    }

    private void addLineDataNoCoverage() {
        addLineData(NO_COVERAGE);
    }

    private void addLineData(int idx) {
        this.lines.add(new LineData(
                idx,
                this.printer.text.get(this.printer.text.size() - 1),
                this.printer.getAndClearLabelReferences(),
                Optional.ofNullable(this.printer.getAndClearLabelDefinition())
        ));
    }

    @Override
    public void visitInsn(int opcode) {
        this.printer.visitInsn(opcode);
        this.addCoverageByteCode();
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        this.printer.visitIntInsn(opcode, operand);
        this.addCoverageByteCode();
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        this.printer.visitVarInsn(opcode, varIndex);
        this.addCoverageByteCode();
        super.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        this.printer.visitTypeInsn(opcode, type);
        this.addCoverageByteCode();
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        this.printer.visitFieldInsn(opcode, owner, name, descriptor);
        this.addCoverageByteCode();
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        this.printer.visitMethodInsn(opcode, owner, name, descriptor);
        this.addCoverageByteCode();
        super.visitMethodInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        this.printer.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        this.addCoverageByteCode();
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        this.printer.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        this.addCoverageByteCode();
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        this.printer.visitJumpInsn(opcode, label);
        this.addCoverageByteCode();
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        this.printer.visitLdcInsn(value);
        this.addCoverageByteCode();
        super.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        this.printer.visitIincInsn(varIndex, increment);
        this.addCoverageByteCode();
        super.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        this.printer.visitTableSwitchInsn(min, max, dflt, labels);
        this.addCoverageByteCode();
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        this.printer.visitLookupSwitchInsn(dflt, keys, labels);
        this.addCoverageByteCode();
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        this.printer.visitMultiANewArrayInsn(descriptor, numDimensions);
        this.addCoverageByteCode();
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.printer.visitTryCatchBlock(start, end, handler, type);
        this.addCoverageByteCode();
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.printer.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        this.addCoverageByteCode();
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.printer.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
        this.addCoverageByteCode();
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    // NON-TRACKED INSTRUCTIONS

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        this.printer.visitLocalVariable(name, descriptor, signature, start, end, index);
        addLineDataNoCoverage();
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        this.printer.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
        addLineDataNoCoverage();
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        this.printer.visitMaxs(maxStack, maxLocals);
        addLineDataNoCoverage();
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        this.printer.visitFrame(type, numLocal, local, numStack, stack);
        addLineDataNoCoverage();
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.printer.visitLineNumber(line, start);
        addLineDataNoCoverage();
        super.visitLineNumber(line, start); // Don't add for line numbers
    }

    @Override
    public void visitLabel(Label label) {
        this.printer.visitLabel(label);
        addLineDataNoCoverage();
        super.visitLabel(label); // Don't add for line numbers
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class LineData {
        /** If the index is < 0 it represents that the line was excluded from coverage, eg: {@link #NO_COVERAGE} */
        public final int idx;
        public final String line;
        public final List<LabelData> labelReferences;
        public final Optional<LabelData> labelDefinition;

        public LineData(int idx, Object element, List<LabelData> labelReferences, Optional<LabelData> labelDefinition) {
            this.idx = idx;
            this.line = elementToString(element);
            this.labelReferences = labelReferences;
            this.labelDefinition = labelDefinition;
        }

        private static String elementToString(Object element) {
            StringBuilder s = new StringBuilder();
            if (element instanceof List) {
                s.append(elementToString(element));
            } else {
                s.append(element);
            }
            return s.toString();
        }
    }

    public static class LabelData {
        public final String name;
        /** The range within the line text that this label takes up */
        public final IntPair range;

        public LabelData(String name, IntPair range) {
            this.name = name;
            this.range = range;
        }
    }
}
