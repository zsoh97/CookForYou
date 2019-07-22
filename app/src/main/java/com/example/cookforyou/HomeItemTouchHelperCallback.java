package com.example.cookforyou;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

public class HomeItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    private static final String TAG = "HomeItemTouchHelper";

    private IngredientAdapter mIngredientAdapter;

    public HomeItemTouchHelperCallback(int dragDir, int swipeDir, IngredientAdapter adapter) {
        super(dragDir, swipeDir);
        mIngredientAdapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        mIngredientAdapter.deleteIngredient(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if(viewHolder != null) {
            View mForegroundView = viewHolder.itemView.findViewById(R.id.viewForeground);
            getDefaultUIUtil().onDrawOver(c, recyclerView, mForegroundView, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View mForegroundView = viewHolder.itemView.findViewById(R.id.viewForeground);
        getDefaultUIUtil().onDraw(c, recyclerView, mForegroundView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if(viewHolder != null) {
            View mForegroundView = viewHolder.itemView.findViewById(R.id.viewForeground);
            getDefaultUIUtil().onSelected(mForegroundView);
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        View mForegroundView = viewHolder.itemView.findViewById(R.id.viewForeground);
        getDefaultUIUtil().clearView(mForegroundView);
    }
}
