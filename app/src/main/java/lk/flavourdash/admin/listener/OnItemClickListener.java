package lk.flavourdash.admin.listener;

import lk.flavourdash.admin.Model.Category;

public interface OnItemClickListener<T> {
    void onItemClick(T item);
}
