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

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableModel;

import java.awt.*;

public class ExtensionNeonmarker extends ExtensionAdaptor {

    @Override
    public String getAuthor() {
        return "Juha Kivekäs";
    }
    
    public void hook(){
        ExtensionHistory extHistory = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
        int idColumnIndex = extHistory.getLogPanel().getHistoryReferenceTable().getModel().getColumnIndex(DefaultHistoryReferencesTableModel.Column.HREF_ID);
        extHistory.getLogPanel().getHistoryReferenceTable().setHighlighters(new MarkItemColorHighlighter(extHistory, idColumnIndex));
    }

    private class MarkItemColorHighlighter extends AbstractHighlighter {
        private int idColumnIndex;
        private ExtensionHistory extHistory;

        public MarkItemColorHighlighter(ExtensionHistory extHistory, int idColumnIndex){
            super();
            setHighlightPredicate(HighlightPredicate.ALWAYS);
            this.extHistory = extHistory;
            this.idColumnIndex = idColumnIndex;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            HistoryReference ref = extHistory.getHistoryReference((int) adapter.getValue(idColumnIndex));
            try{
                if(!ref.getTags().isEmpty()) {
                    component.setBackground(new Color(0xDBD57C));
                }
            }catch(Exception e){}
            return component;
        }
    }
}

