package io.github.notstirred.coverage;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.Textifier;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>WARNING:</h1> This class requires that both {@link #getAndClearLabelDefinition()} and {@link #getAndClearLabelReferences()}
 * are called after each visit method which adds labels is called
 * (eg {@link MethodVisitor#visitIntInsn(int, int)}, {@link MethodVisitor#visitMethodInsn(int, String, String, String, boolean)} etc.)
 * otherwise label references and/or definition will be invalid.
 */
public class LabelRememberingTextifier extends Textifier {
    private final List<CoverageAddingMethodVisitor.LabelData> labelReferences = new ArrayList<>();
    private CoverageAddingMethodVisitor.LabelData labelDefinition = null;

    protected LabelRememberingTextifier(int api) {
        super(api);
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        assert labelReferences.size() == 1 : "Has multiple label references while defining a label";
        this.labelDefinition = this.labelReferences.remove(0);
    }

    @Override
    protected void appendLabel(Label label) {
        int startIdx = this.stringBuilder.length();
        super.appendLabel(label);
        this.labelReferences.add(new CoverageAddingMethodVisitor.LabelData(this.labelNames.get(label), new IntPair(startIdx, this.stringBuilder.length())));
    }

    public List<CoverageAddingMethodVisitor.LabelData> getAndClearLabelReferences() {
        ArrayList<CoverageAddingMethodVisitor.LabelData> references = new ArrayList<>(this.labelReferences);
        this.labelReferences.clear();
        return references;
    }

    public CoverageAddingMethodVisitor.LabelData getAndClearLabelDefinition() {
        CoverageAddingMethodVisitor.LabelData definition = this.labelDefinition;
        this.labelDefinition = null;
        return definition;
    }
}
