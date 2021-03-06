package com.jbrunton.rxandroidexamples.fragments;

import com.jbrunton.rxandroidexamples.TimerFragment;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

public class PersistCacheFragment extends TimerFragment {
    private static Observable<Long> cachedTimer;

    @Override public void onResume() {
        super.onResume();

        if (cachedTimer == null) {
            // Using a ReplaySubject with a capacity of 1 gives us a hot observable that always
            // emits the previous value, so that we don't have to store state across config changes
            // in the fragment. See: http://reactivex.io/documentation/subject.html
            ReplaySubject<Long> subject = ReplaySubject.create(1);

            createTimer().subscribe(subject);

            // Because the cache is static, we persist the observable across config changes. Note
            // that with this simple static variable cache, we can only have one instance of the
            // fragment active at a time.
            cachedTimer = subject;
        }

        cachedTimer
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindToLifecycle())
                .subscribe(onTick);
    }

    @Override public void onDestroy() {
        super.onDestroy();

        if (getActivity().isFinishing()) {
            // Clear the cache only if we're finishing the activity, not if the system is
            // destroying the activity to save memory.
            cachedTimer = null;
        }
    }
}
