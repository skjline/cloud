package com.challenge.ct.mbs.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.challenge.ct.mbs.ContactApplication;
import com.challenge.ct.mbs.R;
import com.challenge.ct.mbs.object.User;
import com.challenge.ct.mbs.ui.fragment.adapter.ContactsRecyclerViewAdapter;
import com.challenge.ct.mbs.utils.Const;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class ListViewFragment extends AbstractFragment {
    private ContactsRecyclerViewAdapter contractView;

    @SuppressWarnings("FieldCanBeLocal")
    private FloatingActionButton fabAdd;

    private SearchView search;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        search = (SearchView) view.findViewById(R.id.sv_name_search);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                          @Override
                                          public boolean onQueryTextSubmit(String query) {
                                              return true;
                                          }

                                          @Override
                                          public boolean onQueryTextChange(String newText) {
                                              // todo: optimize for data usage
                                              presenter.refresh(newText);
                                              return true;
                                          }
                                      }
        );

        contractView = new ContactsRecyclerViewAdapter(listener);
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.rv_contact_list);
        recycler.setAdapter(contractView);

        fabAdd = (FloatingActionButton) view.findViewById(R.id.fab_add_contact);
        fabAdd.setOnClickListener(fab -> {
            if (listener == null) {
                return;
            }

            if (!ContactApplication.isSignedIn()) {
                listener.onEventTriggered(
                        new AbstractFragment.FragmentEvent(Const.GOOGLE_SIGN_IN_REQUEST_CODE));
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putInt(Const.TRANSITION_FRAGMENT_ID, R.layout.fragment_modify);

            listener.onEventTriggered(new AbstractFragment
                    .FragmentEvent(Const.REQUEST_FRAGMENT_INSERT, bundle));
        });

        return view;
    }

    public void onResume() {
        super.onResume();

        presenter.refresh();
    }

    @Override
    public void updateUsers(List<User> users) {
        if (contractView == null || users == null) {
            return;
        }

        contractView.updateUsers(users);
    }
}
