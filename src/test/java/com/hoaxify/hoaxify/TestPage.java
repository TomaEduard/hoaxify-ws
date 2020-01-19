package com.hoaxify.hoaxify;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Data
public class TestPage<T> implements Page<T> {

    long totalElements;
    int totalPages;
    int number;
    int numberOfElements;
    int size;
    boolean last;
    boolean first;
    boolean next;
    boolean previous;

    List<T> content;

    @Override
    public boolean hasContent() {
        return false;
    }

    @Override
    public Sort getSort() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return next;
    }

    @Override
    public boolean hasPrevious() {
        return previous;
    }

    @Override
    public Pageable nextPageable() {
        return null;
    }

    @Override
    public Pageable previousPageable() {
        return null;
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> function) {
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }
}
