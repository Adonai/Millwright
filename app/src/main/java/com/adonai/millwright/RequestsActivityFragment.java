package com.adonai.millwright;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.adonai.millwright.db.AbstractAsyncLoader;
import com.adonai.millwright.db.DbProvider;
import com.adonai.millwright.db.entities.Request;
import com.adonai.millwright.ui.RecycleViewItemClickListener;
import com.adonai.millwright.ui.RequestAdapter;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class RequestsActivityFragment extends Fragment {

    RecyclerView mRecyclerView;
    LoaderManager.LoaderCallbacks<RequestAdapter> mLoaderImpl = new RequestsLoader();

    public RequestsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_requests, container, false);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.request_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        
        getLoaderManager().initLoader(Constants.Loaders.REQUEST_LOADER.ordinal(), Bundle.EMPTY, mLoaderImpl);
        
        return root;
    }

    private class RequestsLoader implements LoaderManager.LoaderCallbacks<RequestAdapter> {
        @Override
        public Loader<RequestAdapter> onCreateLoader(int id, @NonNull final Bundle args) {
            return new AbstractAsyncLoader<RequestAdapter>(getActivity()) {
                @Nullable
                @Override
                public RequestAdapter loadInBackground() {
                    if(!isStarted()) // task was cancelled
                        return null;

                    List<Request> requests = DbProvider.getHelper().getRequestDao().queryForAll();
                    return new RequestAdapter(requests, new RequestClickListener());
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<RequestAdapter> loader, RequestAdapter data) {
            mRecyclerView.setAdapter(data);
        }

        @Override
        public void onLoaderReset(Loader<RequestAdapter> loader) {
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_requests, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_request:
                RequestCrudFragment requestCreate = RequestCrudFragment.newInstance();
                requestCreate.show(getFragmentManager(), "reqCreate");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private class RequestClickListener implements RequestAdapter.ClickListener {

        @Override
        public void onClick(Request request) {
            RequestCompleteFragment rcf = RequestCompleteFragment.newInstance(request);
            rcf.show(getFragmentManager(), "completeRequest");
        }
    }
}
