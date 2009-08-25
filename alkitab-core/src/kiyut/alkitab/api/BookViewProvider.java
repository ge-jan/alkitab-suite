/* This work has been placed into the public domain. */

package kiyut.alkitab.api;

import org.crosswire.jsword.passage.Key;

/**
 * You can register your implementation in META-INF/services or in the layer.xml
 */
public interface BookViewProvider {
    
    /** Open URI
     * @param uri {@link SwordURI} to be opened
     * @param newView only Hints indicating it will be opened in new view or replace existing view
     */
    public void openURI(SwordURI uri, boolean newView);

    /** Open URI
     * @param uri {@link SwordURI} to be opened
     * @param info optional additional info eg: search term, etc
     * @param newView only Hints indicating it will be opened in new view or replace existing view
     */
    public void openURI(SwordURI uri, String info, boolean newView);


    /** Synchronize the All opened bookViewer to display the URI
     * @param key the Key to be displayed
     */
    public void synchronizeView(Key key);
}
