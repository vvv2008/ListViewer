package vvv.listviewer.Activities;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import vvv.listviewer.Model.Item;
import vvv.listviewer.R;

class TestList implements ItemControlHelper.ItemControlInterface {
    private static final boolean DEBUG = true;
    private static final String TAG = "__TestList";

    private Context context;
    private ArrayList<Item> items;
    private ListAdapter listAdapter;
    private RecyclerView recyclerView;
    private ItemControlHelper itemControlHelper;

    TestList(Activity activity, ArrayList<Item> items, boolean vertical) {
        context = activity.getApplicationContext();
        this.items = items;
        listAdapter = new ListAdapter(items, vertical);
        recyclerView = activity.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, vertical ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(listAdapter);
        itemControlHelper = new ItemControlHelper(recyclerView, this, vertical);
    }

    @Override
    public int getItemMovementFlags(int position) {
        Item item = items.get(position);
        int flags = 0;
        if (item.isDragFromStart()) flags |= ItemControlHelper.DRAG_FROM_START;
        if (item.isDragFromEnd()) flags |= ItemControlHelper.DRAG_FROM_END;
        if (item.isSwipeFromStart()) flags |= ItemControlHelper.SWIPE_FROM_START;
        if (item.isSwipeFromEnd()) flags |= ItemControlHelper.SWIPE_FROM_END;
        return flags;
    }

    @Override
    public boolean getSwipeAndControlViews(View[] swipeAndControlViews, RecyclerView.ViewHolder viewHolder, boolean fromStart) {
        Item item = items.get(viewHolder.getAdapterPosition());
        swipeAndControlViews[0] = ((ViewHolder) viewHolder).cardItem;
        swipeAndControlViews[1] = fromStart ? ((ViewHolder) viewHolder).startControls : ((ViewHolder) viewHolder).endControls;
        return item.isPull();
    }

    @Override
    public boolean onItemDragged(int position, int toPosition) {
        if (DEBUG) Log.d(TAG, "onItemDragged() from: " + position + " to " + toPosition);
        Item saved = items.get(toPosition);
        items.set(toPosition, items.get(position));
        items.set(position, saved);
        listAdapter.notifyItemMoved(position, toPosition);
        return true;
    }

    private void onPullClick(int position) {
        if (DEBUG) Log.d(TAG, "onPullClick() pos: " + position);
        items.get(position).togglePull();
        listAdapter.notifyItemChanged(position);
    }

    private void onDragFromStartClick(int position) {
        if (DEBUG) Log.d(TAG, "onDragFromStartClick() pos: " + position);
        items.get(position).toggleDragFromStart();
        listAdapter.notifyItemChanged(position);
    }

    private void onDragFromEndClick(int position) {
        if (DEBUG) Log.d(TAG, "onDragFromEndClick() pos: " + position);
        items.get(position).toggleDragFromEnd();
        listAdapter.notifyItemChanged(position);
    }

    private void onSwipeFromStartClick(int position) {
        if (DEBUG) Log.d(TAG, "onSwipeFromStartClick() pos: " + position);
        items.get(position).toggleSwipeFromStart();
        listAdapter.notifyItemChanged(position);
    }

    private void onSwipeFromEndClick(int position) {
        if (DEBUG) Log.d(TAG, "onSwipeFromEndClick() pos: " + position);
        items.get(position).toggleSwipeFromEnd();
        listAdapter.notifyItemChanged(position);
    }

    private void onItemClick(int position) {
        if (DEBUG) Log.d(TAG, "onItemClick() pos: " + position);
        Snackbar.make(recyclerView, "onItemClick() pos: " + position, Snackbar.LENGTH_LONG).show();
    }

    private void onCtrlStarClick(int position) {
        if (DEBUG) Log.d(TAG, "onCtrlStarClick() pos: " + position);
        Snackbar.make(recyclerView, "onCtrlStarClick() pos: " + position, Snackbar.LENGTH_LONG).show();
    }

    private void onCtrlShareClick(int position) {
        if (DEBUG) Log.d(TAG, "onCtrlShareClick() pos: " + position);
        Snackbar.make(recyclerView, "onCtrlShareClick() pos: " + position, Snackbar.LENGTH_LONG).show();
    }

    private void onCtrlEditClick(int position) {
        if (DEBUG) Log.d(TAG, "onCtrlEditClick() pos: " + position);
        Snackbar.make(recyclerView, "onCtrlEditClick() pos: " + position, Snackbar.LENGTH_LONG).show();
    }

    private void onCtrlDeleteClick(int position) {
        if (DEBUG) Log.d(TAG, "onCtrlDeleteClick() pos: " + position);
        items.remove(position);
        listAdapter.notifyItemRemoved(position);
    }

    private void onCtrlTestClick(int position) {
        if (DEBUG) Log.d(TAG, "onCtrlTestClick() pos: " + position);
        Snackbar.make(recyclerView, "onCtrlTestClick() pos: " + position, Snackbar.LENGTH_LONG).show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private class ListAdapter extends RecyclerView.Adapter<ViewHolder> {
        private ArrayList<Item> items;
        private boolean vertical;

        ListAdapter(ArrayList<Item> items, boolean vertical) {
            this.items = items;
            this.vertical = vertical;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutID = vertical ? R.layout.item_layout_for_vertical_list : R.layout.item_layout_for_horizontal_list;
            View layout = LayoutInflater.from(context).inflate(layoutID, parent, false);
            return new ViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(ViewHolder v, int position) {
            Item item = items.get(position);
            v.itemImage.setImageBitmap(item.getImage());
            v.itemId.setText(item.getStringId());
            v.pull.setChecked(item.isPull());
            v.dragFromStart.setChecked(item.isDragFromStart());
            v.dragFromEnd.setChecked(item.isDragFromEnd());
            v.swipeFromStart.setChecked(item.isSwipeFromStart());
            v.swipeFromEnd.setChecked(item.isSwipeFromEnd());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View cardItem;
        private ImageView itemImage;
        private TextView itemId;
        private SwitchCompat pull;
        private SwitchCompat dragFromStart, dragFromEnd, swipeFromStart, swipeFromEnd;
        private View startControls;
        private View star;
        private View share;
        private View endControls;
        private View edit;
        private View delete;
        private View test;

        ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            cardItem = view.findViewById(R.id.card_item);
            itemImage = cardItem.findViewById(R.id.item_image);
            itemId = cardItem.findViewById(R.id.tv_id);
            pull = cardItem.findViewById(R.id.item_pull);
            pull.setOnClickListener(this);
            dragFromStart = cardItem.findViewById(R.id.drag_from_start);
            dragFromStart.setOnClickListener(this);
            dragFromEnd = cardItem.findViewById(R.id.drag_from_end);
            dragFromEnd.setOnClickListener(this);
            swipeFromStart = cardItem.findViewById(R.id.swipe_from_start);
            swipeFromStart.setOnClickListener(this);
            swipeFromEnd = cardItem.findViewById(R.id.swipe_from_end);
            swipeFromEnd.setOnClickListener(this);
            startControls = view.findViewById(R.id.start_control);
            star = startControls.findViewById(R.id.ib_star);
            star.setOnClickListener(this);
            share = startControls.findViewById(R.id.ib_share);
            share.setOnClickListener(this);
            endControls = view.findViewById(R.id.end_control);
            edit = endControls.findViewById(R.id.ib_edit);
            edit.setOnClickListener(this);
            delete = endControls.findViewById(R.id.ib_delete);
            delete.setOnClickListener(this);
            test = endControls.findViewById(R.id.ib_test);
            test.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemControlHelper.finishSwiping();
            int position = getAdapterPosition();
            if (position == -1) return;
            if (v == itemView) onItemClick(position);
            else if (v == pull) onPullClick(position);
            else if (v == dragFromStart) onDragFromStartClick(position);
            else if (v == dragFromEnd) onDragFromEndClick(position);
            else if (v == swipeFromStart) onSwipeFromStartClick(position);
            else if (v == swipeFromEnd) onSwipeFromEndClick(position);
            else if (v == star) onCtrlStarClick(position);
            else if (v == share) onCtrlShareClick(position);
            else if (v == edit) onCtrlEditClick(position);
            else if (v == delete) onCtrlDeleteClick(position);
            else if (v == test) onCtrlTestClick(position);
        }
    }
}
