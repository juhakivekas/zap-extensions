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
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableModel;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ExtensionNeonmarker extends ExtensionAdaptor {
    private static final Logger logger = Logger.getLogger(ExtensionNeonmarker.class);
    private SortedMap<MappedTag, Color> colourmapping;

    @Override
    public String getAuthor() {
        return "Juha Kivekäs";
    }

    public ExtensionNeonmarker() {
        super();
    }

    public void hook(ExtensionHook extensionHook) {
        ExtensionHistory extHistory = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
        int idColumnIndex = extHistory.getLogPanelHistoryReferenceTable().getModel().getColumnIndex(DefaultHistoryReferencesTableModel.Column.HREF_ID);
        extHistory.getLogPanelHistoryReferenceTable().setHighlighters(new MarkItemColorHighlighter(extHistory, idColumnIndex));
        //TODO make color mapping dynamically configurable from UI
        colourmapping = new TreeMap<>();
        colourmapping.put(new MappedTag("Comment", 1), new Color(0x93FF97));
        colourmapping.put(new MappedTag("Script", 3), new Color(0xFFBBE4));
        colourmapping.put(new MappedTag("foo", 5), new Color(0xF6FF74));
    }

    private class MarkItemColorHighlighter extends AbstractHighlighter {
        private int idColumnIndex;
        private ExtensionHistory extHistory;

        public MarkItemColorHighlighter(ExtensionHistory extHistory, int idColumnIndex) {
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
            if (tags.isEmpty()) {
                return null;
            }
            for (MappedTag mappedtag : colourmapping.keySet()) {
                if (tags.contains(mappedtag.tag)) {
                    return colourmapping.get(mappedtag);
                }
            }
            return null;
        }
    }

    private class MappedTag implements Comparable {
        private String tag;
        private int order;

        public MappedTag(String tag, int order) {
            this.tag = tag;
            this.order = order;
        }

        @Override
        public int compareTo(Object o) {
            return order - ((MappedTag) o).order;
        }
    }
}