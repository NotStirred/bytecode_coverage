package io.github.notstirred.coverage;

import org.objectweb.asm.*;
import org.objectweb.asm.util.Textifier;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASM9;

public class CoverageAddingMethodVisitor extends MethodVisitor {
    private final Textifier printer;

    private final List<LineData> lines = new ArrayList<>();

    protected CoverageAddingMethodVisitor(MethodVisitor methodVisitor) {
        super(ASM9, methodVisitor);
        this.printer = new Textifier(ASM9) { };
    }

    public List<LineData> getLines() {
        return this.lines;
    }

    private void addCoverageByteCode(Object element) {
        lines.add(new LineData(Transformer.idx, element));

        super.visitFieldInsn(Opcodes.GETSTATIC, "io/github/notstirred/coverage/BytecodeCoverageData", "DATA", "[B");
        super.visitLdcInsn(Transformer.idx++);
        super.visitInsn(Opcodes.ICONST_1);
        super.visitInsn(Opcodes.BASTORE);
    }

    @Override
    public void visitInsn(int opcode) {
        this.printer.visitInsn(opcode);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        this.printer.visitIntInsn(opcode, operand);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        this.printer.visitVarInsn(opcode, varIndex);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        this.printer.visitTypeInsn(opcode, type);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        this.printer.visitFieldInsn(opcode, owner, name, descriptor);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        this.printer.visitMethodInsn(opcode, owner, name, descriptor);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitMethodInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        this.printer.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        this.printer.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        this.printer.visitJumpInsn(opcode, label);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        this.printer.visitLdcInsn(value);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        this.printer.visitIincInsn(varIndex, increment);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        this.printer.visitTableSwitchInsn(min, max, dflt, labels);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        this.printer.visitLookupSwitchInsn(dflt, keys, labels);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        this.printer.visitMultiANewArrayInsn(descriptor, numDimensions);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.printer.visitTryCatchBlock(start, end, handler, type);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.printer.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.printer.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
        this.addCoverageByteCode(this.printer.text.get(this.printer.text.size() - 1));
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    // NON-TRACKED INSTRUCTIONS

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        this.printer.visitLocalVariable(name, descriptor, signature, start, end, index);
        this.lines.add(new LineData(-1, this.printer.text.get(this.printer.text.size() - 1)));
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        this.printer.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
        this.lines.add(new LineData(-1, this.printer.text.get(this.printer.text.size() - 1)));
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        this.printer.visitMaxs(maxStack, maxLocals);
        this.lines.add(new LineData(-1, this.printer.text.get(this.printer.text.size() - 1)));
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        this.printer.visitFrame(type, numLocal, local, numStack, stack);
        this.lines.add(new LineData(-1, this.printer.text.get(this.printer.text.size() - 1)));
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.printer.visitLineNumber(line, start);
        this.lines.add(new LineData(-1, this.printer.text.get(this.printer.text.size() - 1)));
        super.visitLineNumber(line, start); // Don't add for line numbers
    }
    @Override
    public void visitLabel(Label label) {
        this.printer.visitLabel(label);
        this.lines.add(new LineData(-1, this.printer.text.get(this.printer.text.size() - 1)));
        super.visitLabel(label); // Don't add for line numbers
    }

    public static class LineData {
        public final int idx;
        public final String line;

        public LineData(int idx, Object element) {
            this.idx = idx;
            this.line = elementToString(element);
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
}
