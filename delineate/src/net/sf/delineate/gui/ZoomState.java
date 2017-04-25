package net.sf.delineate.gui;

/**
 * To store SVG canvas zoom state.
 */
public class ZoomState {
    /**
     * When 0 not zoomed.
     * When positive zoomed in that many times.
     * When negative zoomed out that many times.
     */
    private int zoom;

    public ZoomState() {
        this.zoom = 0;
    }

    public void zoomIn() {
        this.zoom += 1;
    }

    public void zoomOut() {
        this.zoom -= 1;
    }

    public int getZoom() {
        return zoom;
    }

    public boolean zoomedIn() {
        return zoom > 0;
    }

    public boolean zoomedOut() {
        return zoom < 0;
    }

    public boolean isZoomed() { return zoomedIn() || zoomedOut(); }
}
