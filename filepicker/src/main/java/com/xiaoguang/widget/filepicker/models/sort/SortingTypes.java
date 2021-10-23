package com.xiaoguang.widget.filepicker.models.sort;

import com.xiaoguang.widget.filepicker.models.Document;

import java.util.Comparator;


/**
 * Created by gabriel on 10/2/17.
 */

public enum SortingTypes {
    name(new NameComparator()), none(null);

    final private Comparator<Document> comparator;

    SortingTypes(Comparator<Document> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Document> getComparator() {
        return comparator;
    }
}
