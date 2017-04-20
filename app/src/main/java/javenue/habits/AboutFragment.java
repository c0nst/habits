package javenue.habits;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity context = getActivity();

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView versionView = (TextView) view.findViewById(R.id.versionName);
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionView.setText(info.versionName);
        } catch (PackageManager.NameNotFoundException ignore) { }

        return view;
    }
}
