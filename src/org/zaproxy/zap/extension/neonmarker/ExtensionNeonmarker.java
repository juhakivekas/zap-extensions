/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.neonmarker;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableModel;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ExtensionNeonmarker extends ExtensionAdaptor {
    private static final Logger logger = Logger.getLogger(ExtensionNeonmarker.class);
    private ArrayList<ColorMapping> colormap;
    private NeonmarkerPanel neonmarkerPanel;

    static Color[] palette = {
            //RAINBOW HACKER THEME
            new Color(0xff8080),
            new Color(0xffc080),
            new Color(0xffff80),
            new Color(0xc0ff80),
            new Color(0x80ff80),
            new Color(0x80ffc0),
            new Color(0x80ffff),
            new Color(0x80c0ff),
            new Color(0x8080ff),
            new Color(0xc080ff),
            new Color(0xff80ff),
            new Color(0xff80c0),
            //CORPORATE EDITION
            new Color(0xe0ffff),
            new Color(0xa8c0c0),
            new Color(0x708080),
            new Color(0x384040)
    };

    @Override
    public String getAuthor() {
        return "Juha Kivekäs";
    }

    public ExtensionNeonmarker() {
        super();
    }

    public void hook(ExtensionHook extensionHook) {
        ExtensionHistory extHistory = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
        int idColumnIndex = extHistory.getHistoryReferencesTable().getModel().getColumnIndex(DefaultHistoryReferencesTableModel.Column.HREF_ID);
        extHistory.getHistoryReferencesTable().setHighlighters(new MarkItemColorHighlighter(extHistory, idColumnIndex));

        colormap = new ArrayList<>();

        ExtensionHookView hookView = extensionHook.getHookView();
        hookView.addStatusPanel(getNeonmarkerPanel());
    }

    private NeonmarkerPanel getNeonmarkerPanel() {
        if (neonmarkerPanel == null) {
            ExtensionHistory extHistory = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
            neonmarkerPanel = new NeonmarkerPanel(extHistory.getModel(), colormap);
        }
        return neonmarkerPanel;
    }

    private class MarkItemColorHighlighter extends AbstractHighlighter {
        private int idColumnIndex;
        private ExtensionHistory extHistory;

        MarkItemColorHighlighter(ExtensionHistory extHistory, int idColumnIndex) {
            super();
            setHighlightPredicate(HighlightPredicate.ALWAYS);
            this.extHistory = extHistory;
            this.idColumnIndex = idColumnIndex;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            HistoryReference ref = extHistory.getHistoryReference((int) adapter.getValue(idColumnIndex));
            List<String> tags;
            try {
                tags = ref.getTags();
            } catch (Exception e) {
                logger.error("Failed to fetch tags for history reference");
                return component;
            }

            Color mark = mapTagsToColor(tags);
            if (mark != null) {
                component.setBackground(mark);
            }
            return component;
        }

        private Color mapTagsToColor(List<String> tags) {
            for (ColorMapping colorMapping : colormap) {
                if (tags.contains(colorMapping.tag)) {
                    return colorMapping.color;
                }
            }
            return null;
        }
    }

    static class ColorMapping {
        public String tag;
        public Color color;

        ColorMapping(){
            this.tag = null;
            this.color = palette[0];
        }
    }
}