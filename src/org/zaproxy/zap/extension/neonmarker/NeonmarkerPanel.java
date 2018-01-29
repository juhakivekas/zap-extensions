package org.zaproxy.zap.extension.neonmarker;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class NeonmarkerPanel extends AbstractPanel {
    private static final ImageIcon neonmarkerIcon;
    private Model historyTableModel;
    private ArrayList<ExtensionNeonmarker.ColorMapping> colormap;
    private Container colorSelectionPanel;
    private JToolBar toolbar;
    private JButton clearButton, addButton;

    static {
        neonmarkerIcon = new ImageIcon(NeonmarkerPanel.class.getResource("/org/zaproxy/zap/extension/neonmarker/resources/spectrum.png"));
    }

    NeonmarkerPanel(Model model, ArrayList<ExtensionNeonmarker.ColorMapping> colormap) {
        historyTableModel = model;
        this.colormap = colormap;
        initializePanel();
    }

    private void initializePanel() {
        setName(Constant.messages.getString("neonmarker.panel.title"));
        setIcon(neonmarkerIcon);
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        add(getPanelToolbar(), constraints);

        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        colorSelectionPanel = new JPanel();
        colorSelectionPanel.setLayout(new BoxLayout(colorSelectionPanel, BoxLayout.PAGE_AXIS));
        JScrollPane colorSelectionPanelScrollFrame = new JScrollPane();
        colorSelectionPanelScrollFrame.setPreferredSize(new Dimension(800, 300));
        colorSelectionPanelScrollFrame.setViewportView(colorSelectionPanel);
        add(colorSelectionPanelScrollFrame, constraints);
        clearColorSelectionPanel();
    }

    private Component getPanelToolbar() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setEnabled(true);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setPreferredSize(new java.awt.Dimension(800, 30));
            toolbar.add(getClearButton());
            toolbar.add(getAddButton());
        }
        return toolbar;
    }

    private Component getClearButton() {
        if (clearButton == null) {
            clearButton = new JButton();
            clearButton.setEnabled(true);
            clearButton.setIcon(new ImageIcon(NeonmarkerPanel.class.getResource("/resource/icon/fugue/broom.png")));
            clearButton.setToolTipText(Constant.messages.getString("neonmarker.panel.button.clear"));
            clearButton.addActionListener(actionEvent -> {
                clearColorSelectionPanel();
                refreshColormap();
            });
        }
        return clearButton;
    }

    private Component getAddButton() {
        if (addButton == null) {
            addButton = new JButton(/*make a label?*/);
            addButton.setEnabled(true);
            addButton.setIcon(new ImageIcon(NeonmarkerPanel.class.getResource("/resource/icon/16/103.png")));
            addButton.setToolTipText(Constant.messages.getString("neonmarker.panel.button.add"));
            addButton.addActionListener(actionEvent -> colorSelectionPanel.add(new ColorMappingRow()));
        }
        return addButton;
    }

    private void clearColorSelectionPanel() {
        colorSelectionPanel.removeAll();
        colorSelectionPanel.add(new ColorMappingRow());
    }

    private void refreshColormap() {
        colormap.clear();
        for (Component c : colorSelectionPanel.getComponents()) {
            if (c instanceof ColorMappingRow) {
                colormap.add(new ExtensionNeonmarker.ColorMapping(
                        ((ColorMappingRow) c).selectedTag,
                        ((ColorMappingRow) c).selectedColor));
            }
        }
    }

    private class ColorMappingRow extends JPanel {
        String selectedTag;
        Color selectedColor;
        private JComboBox<String> tagSelect;
        private JComboBox<Color> colorSelect;
        private JButton deleteButton;

        ColorMappingRow() {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(getDeleteButton());
            add(getTagSelect());
            add(getColorSelect());
        }

        private Component getDeleteButton() {
            deleteButton = new JButton("");
            deleteButton.setPreferredSize(new Dimension(30, 24));
            deleteButton.setEnabled(true);
            deleteButton.setIcon(new ImageIcon(NeonmarkerPanel.class.getResource("/resource/icon/16/104.png")));
            deleteButton.setToolTipText(Constant.messages.getString("neonmarker.panel.mapping.delete"));
            deleteButton.addActionListener(actionEvent -> {
                colorSelectionPanel.remove(this);
                colorSelectionPanel.repaint();
                refreshColormap();
            });
            return deleteButton;
        }

        private Component getTagSelect() {
            TagListModel tagListModel = new TagListModel();
            tagSelect = new JComboBox<>(tagListModel);
            tagSelect.setPreferredSize(new Dimension(200, 24));
            tagSelect.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                    ((TagListModel) tagSelect.getModel()).updateTags();
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
                }
            });
            tagSelect.addActionListener(actionEvent -> {
                selectedTag = (String) tagSelect.getSelectedItem();
                refreshColormap();
            });
            return tagSelect;
        }

        private Component getColorSelect() {
            colorSelect = new JComboBox<>(ExtensionNeonmarker.palette);
            colorSelect.setPreferredSize(new Dimension(100, 24));
            colorSelect.setRenderer(new ColorListRenderer());
            colorSelect.addActionListener(actionEvent -> {
                selectedColor = (Color) colorSelect.getSelectedItem();
                refreshColormap();
            });
            return colorSelect;
        }
    }

    private class TagListModel implements ComboBoxModel<String> {
        private List<String> allTags;
        private ArrayList<ListDataListener> listDataListeners;
        private Object selectedItem;

        TagListModel() {
            listDataListeners = new ArrayList<>();
            updateTags();
        }

        private void updateTags() {
            try {
                allTags = historyTableModel.getDb().getTableTag().getAllTags();
            } catch (Exception e) {
                //do nothing
            }
            if (allTags.isEmpty()) {
                allTags.add("No tags found");
            }
            for (ListDataListener l : listDataListeners) {
                l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, allTags.size() - 1));
            }
        }

        @Override
        public int getSize() {
            return allTags.size();
        }

        @Override
        public String getElementAt(int i) {
            return allTags.get(i);
        }

        @Override
        public void addListDataListener(ListDataListener listDataListener) {
            listDataListeners.add(listDataListener);
        }

        @Override
        public void removeListDataListener(ListDataListener listDataListener) {
            listDataListeners.remove(listDataListener);
        }

        @Override
        public void setSelectedItem(Object o) {
            selectedItem = o;
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }
    }

    private class ColorListRenderer extends JLabel implements ListCellRenderer<Color> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Color> jList, Color color, int i, boolean b, boolean b1) {
            BufferedImage img = new BufferedImage(100, 16, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = img.createGraphics();
            graphics.setColor(color);
            graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
            setIcon(new ImageIcon(img));
            return this;
        }
    }
}