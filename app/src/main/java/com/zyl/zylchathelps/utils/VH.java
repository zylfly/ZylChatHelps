package com.zyl.zylchathelps.utils;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VH extends RecyclerView.ViewHolder {

    public VH(View itemView) {
        super(itemView);
    }

    public void bindData(Object o,int pos) {
    }

    protected <T extends View> T $(@IdRes int id) {
        return (T) itemView.findViewById(id);
    }

}