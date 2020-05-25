package eu.gebes.utils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionSelector<Type> {

    final List<Option> options = new LinkedList<>();

    public OptionSelector<Type> addOption(String title, Type selectedItem) {
        options.add(new Option(title, selectedItem));
        return this;
    }


    public Type select(String prefix) {


        for (int i = 0; i < options.size(); i++)
            System.out.println("\t" + (i + 1) + ": " + options.get(i).getTitle());

        int selectedIndex = ConsoleUtils.scanIntRange(prefix, 1, options.size()) - 1;

        return options.get(selectedIndex).getSelectedItem();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Data
    @RequiredArgsConstructor
    class Option {

        @NonNull String title;
        @NonNull Type selectedItem;

    }

}
