package com.bypassmobile.octo.utils;

import com.flesh.webservice.model.User;

import java.util.Comparator;

/**
 * Created by aaronfleshner on 1/11/17.
 */

public class SortingUtils {

    public static class UserAlphabeticalComparetor implements Comparator<User> {

        @Override
        public int compare(User user, User nextUser) {
            return user.getName().compareToIgnoreCase(nextUser.getName());
        }
    }


}
