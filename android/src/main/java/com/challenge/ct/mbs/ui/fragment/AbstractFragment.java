package com.challenge.ct.mbs.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.challenge.ct.mbs.ContactApplication;
import com.challenge.ct.mbs.aws.ContactContract;
import com.challenge.ct.mbs.aws.ContactDataPresenter;

public abstract class AbstractFragment extends Fragment implements ContactContract.View {
    private static final int COLOR_ACTION_MENU = Color.WHITE;

    protected FragmentEventListener listener;
    protected ContactDataPresenter presenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof FragmentEventListener) {
            listener = (FragmentEventListener) getActivity();
        }

        presenter = new ContactDataPresenter(this, ContactApplication.getDatabase());
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // ensures clearing a listener
        listener = null;
    }

    /**
     * Applies common menu styling on options menu items,
     * Particularly to normalize the colors of options menu item for a consistent look
     *
     * @param menuIds options menu items to apply common styling
     * @param menu    Inflated options menu
     */
    protected void applyOptionsMenuStyle(int[] menuIds, Menu menu) {
        if (menuIds == null || menuIds.length <= 0 || menu == null || menu.size() <= 0) {
            return;
        }

        MenuItem item;
        for (int id : menuIds) {
            if ((item = menu.findItem(id)) == null) {
                continue;
            }

            item.getIcon().setColorFilter(COLOR_ACTION_MENU, PorterDuff.Mode.SRC_ATOP);
        }
    }

    protected void triggerFragmentRequest(FragmentEvent event) {
        if (listener == null) {
            return;
        }

        listener.onEventTriggered(event);
    }

    public void refresh() {
        if (presenter != null) {
            presenter.refresh();
        }
    }

    public static class FragmentEvent {
        private final int type;
        private final Object message;

        public FragmentEvent(int type) {
            this(type, null);
        }

        public FragmentEvent(int type, Object message) {
            this.type = type;
            this.message = message;
        }

        public int getType() {
            return type;
        }

        public Object getMessage() {
            return message;
        }
    }

    public interface FragmentEventListener {
        void onEventTriggered(FragmentEvent event);
    }
}
