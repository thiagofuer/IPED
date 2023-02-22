package iped.app.home.configurables;

import java.awt.Component;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.tika.mime.MediaType;

import iped.app.ui.IconManager;
import iped.engine.data.Category;

public class CheckBoxTreeCellRenderer implements TreeCellRenderer{
    JCheckBox checkbox=new JCheckBox();
    JLabel label = new JLabel();
    private JTree tree;
    Predicate<Object> checkedPredicate;
    Predicate<Object> visiblePredicate;

    public CheckBoxTreeCellRenderer(JTree tree, Predicate<Object> checkedPredicate) {
        this.tree = tree;
        this.checkedPredicate = checkedPredicate;
        
        TreeSelectionModel selModel = tree.getSelectionModel();
        
        selModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        selModel.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if(e.isAddedPath()) {
                    Object value = e.getNewLeadSelectionPath().getLastPathComponent();
                    if(visiblePredicate==null || visiblePredicate.test(value)) {
                        checkbox.setSelected(!checkbox.isSelected());
                        tree.getSelectionModel().clearSelection();
                    }
                }
            }
        });        
    }

    public CheckBoxTreeCellRenderer(JTree tree, Predicate<Object> checkedPredicate, Predicate<Object> visiblePredicate) {
        this(tree, checkedPredicate);
        this.visiblePredicate = visiblePredicate;        
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        if(row==-1) {
            label.setText("");
            return label;
        }
        
        JComponent result = null;

        Icon icon = null;
        if(value instanceof MediaType) {
            icon = IconManager.getFileIcon(value.toString().split("/")[0], "");
        }
        if(value instanceof Category) {
            icon = IconManager.getCategoryIcon(((Category)value).getName().toLowerCase());
        }

        TreePath tp = tree.getPathForRow(row);

        if(visiblePredicate==null || visiblePredicate.test(value)) {
            checkbox.setText(value.toString());
            checkbox.setSelected(checkedPredicate.test(value));
            checkbox.setIcon(icon);

            result = checkbox;
        }else{
            label.setText(value.toString());
            label.setIcon(icon);
            result = label;
        }

        return result;
    }
}