package com.challenge.ct.mbs.ui;

import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.ui.fragment.AbstractFragment;
import com.challenge.ct.mbs.ui.fragment.DetailViewFragment;
import com.challenge.ct.mbs.ui.fragment.ListViewFragment;
import com.challenge.ct.mbs.ui.fragment.ModifyFragment;
import com.challenge.ct.mbs.utils.Const;

public class FragmentFactory {
    public static AbstractFragment getFragment(int id) {
        AbstractFragment fragment;
        switch (id) {
            case R.layout.fragment_detail:
                fragment = new DetailViewFragment();
                break;
            case R.layout.fragment_contacts:
                fragment = new ListViewFragment();
                break;
            case R.layout.fragment_modify:
                fragment = new ModifyFragment();
                break;
            default:
                throw new IllegalArgumentException(Const.ERROR_INVALID_FRAGMENT);
        }

        return fragment;
    }
}
