package vvv.listviewer.Model;

import android.graphics.Bitmap;

public class Item {
    private int id;
    private Bitmap image;
    private boolean dragFromStart = false;
    private boolean dragFromEnd = false;
    private boolean swipeFromStart = false;
    private boolean swipeFromEnd = false;
    private boolean pull = false;

    public Item(int id, Bitmap image) {
        this.id = id;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getStringId() {
        return "Item ID: " + id;
    }

    public Bitmap getImage() {
        return image;
    }

    public boolean isDragFromStart() {
        return dragFromStart;
    }

    public void toggleDragFromStart() {
        dragFromStart = !dragFromStart;
    }

    public boolean isDragFromEnd() {
        return dragFromEnd;
    }

    public void toggleDragFromEnd() {
        dragFromEnd = !dragFromEnd;
    }

    public boolean isSwipeFromStart() {
        return swipeFromStart;
    }

    public void toggleSwipeFromStart() {
        swipeFromStart = !swipeFromStart;
    }

    public boolean isSwipeFromEnd() {
        return swipeFromEnd;
    }

    public void toggleSwipeFromEnd() {
        swipeFromEnd = !swipeFromEnd;
    }

    public boolean isPull() {
        return pull;
    }

    public void togglePull() {
        pull = !pull;
    }

}
