/* This work has been placed into the public domain. */
        
package kiyut.alkitab.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import kiyut.alkitab.api.BookViewManager;
import kiyut.alkitab.api.SwordURI;
import kiyut.alkitab.options.BookViewerOptions;
import kiyut.alkitab.bookviewer.DictionaryPane;
import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.book.BookFilter;
import org.crosswire.jsword.book.BookFilters;
import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.passage.Key;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays {@link kiyut.alkitab.swing.DictionaryPane DictionaryPane}.
 */
public final class DefinitionsTopComponent extends TopComponent {

    private static DefinitionsTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    private static final String PREFERRED_ID = "DefinitionsTopComponent";
    
    private JTabbedPane tabbedPane;
    
    private HyperlinkListener hyperlinkListener;

    protected PropertyChangeListener backgroundPropertyChangeListener;
    
    private DefinitionsTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DefinitionsTopComponent.class, "CTL_DefinitionsTopComponent"));
        setToolTipText(NbBundle.getMessage(DefinitionsTopComponent.class, "HINT_DefinitionsTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        
        initCustom();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DefinitionsTopComponent getDefault() {
        if (instance == null) {
            instance = new DefinitionsTopComponent();
        } 
        return instance;
    }

    /**
     * Obtain the DefinitionsTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DefinitionsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DefinitionsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DefinitionsTopComponent) {
            return (DefinitionsTopComponent) win;
        }
        Logger.getLogger(DefinitionsTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            //return DefinitionsTopComponent.getDefault();
            
            final DefinitionsTopComponent result = DefinitionsTopComponent.getDefault();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // always close it at startup
                    result.close();
                }
            });
            return result;
        }
    } 
    
    @Override
    public javax.swing.Action[] getActions() {
        List<Action> actionList = new ArrayList<Action>();
        
        // add 
        actionList.add(new ViewSourceAction());
        actionList.add(null);  // separator
        actionList.addAll(Arrays.asList(super.getActions()));
        
        return actionList.toArray(new Action[0]);
    }
    
    private void initCustom() {
        tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
        tabbedPane.addPropertyChangeListener( TabbedPaneFactory.PROP_CLOSE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                JTabbedPane pane = (JTabbedPane)evt.getSource();
                Object obj = evt.getNewValue();
                if (obj == null || !(obj instanceof DictionaryPane)) {
                    return;
                }
                DictionaryPane dicPane = (DictionaryPane)obj;
                dicPane.removeHyperlinkListener(hyperlinkListener);
                pane.remove(dicPane);
            }
        });
        this.add(BorderLayout.CENTER, tabbedPane);
        
        hyperlinkListener = new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                DefinitionsTopComponent.this.hyperlinkUpdate(evt);
            }
        };

        backgroundPropertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!evt.getPropertyName().equals(BookViewerOptions.BACKGROUND)) {
                    return;
                }

                Color bg = null;
                if (evt.getNewValue() instanceof Color) {
                    bg = (Color) evt.getNewValue();
                }

                backgroundPropertyChange(bg);
            }
        };

        // this part make the loading slower
        BookViewerOptions opts = BookViewerOptions.getInstance();
        opts.addPropertyChangeListener(backgroundPropertyChangeListener);
        
        String name = opts.getDefaultDictionary();
        if (name == null) { return; }
        SwordURI uri = SwordURI.createURI(SwordURI.DICTIONARY_SCHEME, name, null);
        openURI(uri, null);
    }
    
    public void openURI(SwordURI uri, String info) {
        if (uri.getType() != SwordURI.Type.DICTIONARY 
                && uri.getType() != SwordURI.Type.GLOSSARY
                && uri.getType() != SwordURI.Type.GREEK_STRONGS
                && uri.getType() != SwordURI.Type.HEBREW_STRONGS
                && uri.getType() != SwordURI.Type.GREEK_MORPH) { 
            return; 
        }

        String name = uri.getPath();
        if (name.equals("") ) {
            switch (uri.getType()) {
                case GREEK_STRONGS:
                    name = getValidBookName(BookViewerOptions.getInstance().getDefaultGreekStrongs(), BookFilters.getGreekDefinitions());
                    break;
                case HEBREW_STRONGS:
                    name = getValidBookName(BookViewerOptions.getInstance().getDefaultHebrewStrongs(), BookFilters.getHebrewDefinitions());
                    break;
                case GREEK_MORPH:
                    name = getValidBookName(BookViewerOptions.getInstance().getDefaultGreekMorph(), BookFilters.getGreekParse());
                    break;
                default:
                    break;
            }
        }
        
        Book book = (name != null ? Books.installed().getBook(name) : null);
        if (book == null) {
            return;
        }
        
        int index = -1;
        
        // find if the dictionary already opened or not
        for (int i=0; i<tabbedPane.getTabCount(); i++) {
            DictionaryPane dicPane = (DictionaryPane)tabbedPane.getComponentAt(i);
            if (dicPane.getBook().getInitials().equals(book.getInitials())) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            DictionaryPane dicPane = new DictionaryPane(book);
            dicPane.addHyperlinkListener(hyperlinkListener);
            dicPane.setName(book.getInitials());
            
            // do not use addTab, there is bug for space between title and x button
            //tabbedPane.addTab(book.getInitials() + "   ", null, dicPane, book.getName());
            tabbedPane.add(dicPane);
            index = tabbedPane.getTabCount() - 1;
            tabbedPane.setToolTipTextAt(index, book.getName());

            Color bg = BookViewerOptions.getInstance().getBackground();
            backgroundPropertyChange(bg);
        }
        
        tabbedPane.setSelectedIndex(index);
        
        String keyString = uri.getFragment();
        if (keyString != null && !keyString.equals("")) {
            DictionaryPane dicPane = (DictionaryPane)tabbedPane.getSelectedComponent();
            Book theBook = dicPane.getBook();
            if (book != null) {
                Key key = theBook.getValidKey(keyString);
                dicPane.setKey(key);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String getValidBookName(String name, BookFilter filter) {
        boolean found = false;
        List<Book> books = Books.installed().getBooks(filter);
        
        if (name != null) {
            for (int i=0; i<books.size(); i++) {
                if (books.get(i).getInitials().equalsIgnoreCase(name)) {
                    found = true;
                    break;
                }
            }
        }
        
        if (!found && !books.isEmpty()) {
                name = books.get(0).getInitials();
        }
        
        return name;
    }
    
    protected void hyperlinkUpdate(HyperlinkEvent evt) {
        EventType eventType = evt.getEventType();
        String uri = evt.getDescription();
        SwordURI swordURI = SwordURI.createURI(uri);
        
        if (swordURI == null) {
            Logger logger = Logger.getLogger(DefinitionsTopComponent.class.getName());
            logger.log(Level.WARNING, "invalid SwordURI: {0}", uri);
            
        }
        
        if (eventType.equals(HyperlinkEvent.EventType.ACTIVATED)) {
            String fragment = swordURI.getFragment();
            if (fragment.length() > 0) {
                if (fragment.charAt(0) == '#') {
                    return;
                }
            }
            
            BookViewManager.getInstance().openURI(swordURI);
        } else if (eventType.equals(HyperlinkEvent.EventType.ENTERED)) {
            //StatusDisplayer.getDefault().setStatusText(uri);
            StatusDisplayer.getDefault().setStatusText(swordURI.toString());
        }
    }

    protected void backgroundPropertyChange(Color bg) {
        for (int i=0; i<tabbedPane.getTabCount(); i++) {
            DictionaryPane dicPane = (DictionaryPane)tabbedPane.getComponentAt(i);
            dicPane.getViewerComponent().setBackground(bg);
        }
    }
    
    private class ViewSourceAction extends AbstractAction {
        public ViewSourceAction() {
            putValue(Action.NAME, NbBundle.getMessage(ViewSourceAction.class, "CTL_ViewSourceAction"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            int i = tabbedPane.getSelectedIndex();
            if (i < 0) { return; }
            
            DictionaryPane dicPane = (DictionaryPane)tabbedPane.getComponentAt(i);
            dicPane.viewSource();
        }
    }
}
