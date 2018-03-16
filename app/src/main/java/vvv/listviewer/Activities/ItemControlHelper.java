package vvv.listviewer.Activities;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

class ItemControlHelper extends ItemTouchHelper.Callback {
    static final int DRAG_FROM_START = 1, DRAG_FROM_END = 2, SWIPE_FROM_START = 4, SWIPE_FROM_END = 8;

    interface ItemControlInterface {
        int getItemMovementFlags(int position);

        boolean getSwipeAndControlViews(View[] swipeAndControlViews, RecyclerView.ViewHolder viewHolder, boolean fromStart);

        boolean onItemDragged(int position, int toPosition);
    }

    private static final boolean DEBUG = false;
    private static final String TAG = "__ItemControlHelper";
    private ItemControlInterface controlInterface;
    private boolean vertical;
    private int flagDragFromStart, flagDragFromEnd, flagSwipeFromStart, flagSwipeFromEnd;
    // For swiping
    private int flags;
    private RecyclerView.ViewHolder swipingViewHolder = null;
    private View swipingView;
    private View controlView;
    private boolean pullControls;
    private boolean swipingFromStart;
    private float controlSize;
    private float xLeft, xLeftTarget, animationProportionAndSign, dXOld;

    ItemControlHelper(RecyclerView recyclerView, ItemControlInterface controlInterface, boolean vertical) {
        this.controlInterface = controlInterface;
        this.vertical = vertical;
        if (vertical) {
            flagDragFromStart = ItemTouchHelper.DOWN;
            flagDragFromEnd = ItemTouchHelper.UP;
            flagSwipeFromStart = ItemTouchHelper.RIGHT;
            flagSwipeFromEnd = ItemTouchHelper.LEFT;
        } else {
            flagDragFromStart = ItemTouchHelper.RIGHT;
            flagDragFromEnd = ItemTouchHelper.LEFT;
            flagSwipeFromStart = ItemTouchHelper.DOWN;
            flagSwipeFromEnd = ItemTouchHelper.UP;
        }
        new ItemTouchHelper(this).attachToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (swipingViewHolder != null && newState == RecyclerView.SCROLL_STATE_DRAGGING)
                    finishSwiping();
            }
        });
    }

    void finishSwiping() {
        finishDrawingSwiping();
        controlView = null;
        swipingView = null;
        swipingViewHolder = null;
        if (DEBUG) Log.d(TAG, "Control CLOSED");
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (DEBUG)
            Log.d(TAG, "onMove() from: " + viewHolder.getAdapterPosition() + " to: " + target.getAdapterPosition());
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();
        return from != -1 && to != -1 && to != from && controlInterface.onItemDragged(from, to);
    }

    /**
     * Off swipe threshold
     */
    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return Float.MAX_VALUE;
    }

    /**
     * Off swipe velocity
     */
    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return Float.MAX_VALUE;
    }

    /**
     * Swipe always Off
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) Log.d(TAG, "getMovementFlags() on position: " + viewHolder.getAdapterPosition());
        if (viewHolder.getAdapterPosition() == -1) return 0;
        if (swipingViewHolder != null && viewHolder != swipingViewHolder) finishSwiping();
        int dragFlags = 0;
        int swipeFlags = 0;
        if (viewHolder == swipingViewHolder) swipeFlags = flagSwipeFromStart | flagSwipeFromEnd;
        else {
            flags = controlInterface.getItemMovementFlags(viewHolder.getAdapterPosition());
            if ((flags & DRAG_FROM_START) != 0) dragFlags |= flagDragFromStart;
            if ((flags & DRAG_FROM_END) != 0) dragFlags |= flagDragFromEnd;
            if ((flags & SWIPE_FROM_START) != 0) swipeFlags |= flagSwipeFromStart;
            if ((flags & SWIPE_FROM_END) != 0) swipeFlags |= flagSwipeFromEnd;
        }
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (DEBUG)
            Log.d(TAG, "onChildDraw() isCurrentlyActive: " + isCurrentlyActive + " actionState: " + actionState + " dX: " + dX + " dY: " + dY);

        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) { // Not swiping
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        // Swiping

        if (viewHolder == null || viewHolder.getAdapterPosition() == -1) return;

        if (swipingViewHolder != null && viewHolder != swipingViewHolder)
            return; // Another viewHolder

        dX = vertical ? dX : dY;

        if (isCurrentlyActive) { // Swipe manually

            if (swipingViewHolder == null) { // First touch
                if (dX == 0F) return; // While direction undetected
                startSwiping(viewHolder, dX);
                xLeft = dXOld = 0F;
            }

            xLeft += dX - dXOld;

            if ((swipingFromStart && xLeft < 0F) || (!swipingFromStart && xLeft > 0F)) { // Direction changed
                finishSwiping();
                startSwiping(viewHolder, xLeft);
            }

            if (Math.abs(xLeft) > controlSize * .5F) // Opened > 50% of controlView
                xLeftTarget = swipingFromStart ? controlSize : -controlSize;
            else xLeftTarget = 0F;

            // To automatically open the controlView, use a simple proportion,
            // although this changes the speed of the animation
            if (dX != 0F) animationProportionAndSign = (xLeft - xLeftTarget) / dX;

        } else { // Swipe animation

            if (dX == 0F) xLeft = xLeftTarget;
            else xLeft += (dX - dXOld) * animationProportionAndSign;

        }

        dXOld = dX;

        if (xLeft == 0F && swipingViewHolder != null)
            finishSwiping(); // Now controlView fully closed
        else drawSwiping(xLeft);
    }

    private void startSwiping(RecyclerView.ViewHolder viewHolder, float dX) {
        swipingViewHolder = viewHolder;
        swipingFromStart = dX > 0F;
        View[] swipeAndControlViews = new View[2];
        if ((swipingFromStart && (flags & SWIPE_FROM_START) != 0) || (!swipingFromStart && (flags & SWIPE_FROM_END) != 0))
            pullControls = controlInterface.getSwipeAndControlViews(swipeAndControlViews, viewHolder, swipingFromStart);
        swipingView = swipeAndControlViews[0];
        controlView = swipeAndControlViews[1];
        prepareDrawingSwiping();
        if (DEBUG) Log.d(TAG, "Control OPENED from " + (swipingFromStart ? "START" : "END"));
    }

    /**
     * Good place for experiments with animation
     */

    private void prepareDrawingSwiping() {
        if (controlView != null) {
            controlView.setVisibility(View.VISIBLE);
            controlSize = vertical ? controlView.getWidth() : controlView.getHeight();
        } else controlSize = Float.MAX_VALUE;
    }

    private void drawSwiping(float x) {
        if (swipingView != null) {
            if (vertical) swipingView.setTranslationX(x);
            else swipingView.setTranslationY(x);
        }
        if (controlView != null && pullControls) {
            float size = swipingFromStart ? -controlSize : controlSize;
            if (vertical) controlView.setTranslationX(size + x);
            else controlView.setTranslationY(size + x);
        }
    }

    private void finishDrawingSwiping() {
        if (controlView != null) {
            if (pullControls) {
                if (vertical) controlView.setTranslationX(0F);
                else controlView.setTranslationY(0F);
            }
            controlView.setVisibility(View.INVISIBLE);
        }
        if (swipingView != null) {
            if (vertical) swipingView.setTranslationX(0F);
            else swipingView.setTranslationY(0F);
        }
    }
}
