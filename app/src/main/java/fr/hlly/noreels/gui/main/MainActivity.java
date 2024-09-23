package fr.hlly.noreels.gui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import fr.hlly.noreels.R;
import fr.hlly.noreels.action.ActionType;
import fr.hlly.noreels.db.Rule;
import fr.hlly.noreels.db.RuleDataSource;
import fr.hlly.noreels.db.RuleObserver;
import fr.hlly.noreels.gui.rule.EditRuleActivity;
import fr.hlly.noreels.gui.rule.NewRuleActivity;
import fr.hlly.noreels.service.A11yService;

/**
 * The main activity that is showing a list of rules.
 *
 * @author Niklaus Leuenberger
 */
public class MainActivity extends AppCompatActivity implements RuleObserver, RuleRecyclerViewAdapter.ItemClickListener {

    RuleRecyclerViewAdapter adapter;
    private RuleDataSource data;
    ArrayList<Rule> rules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the RecyclerView with an Adapter that has a list of rules. The list of rules is
        // managed by the RuleObserver callbacks implemented further down.
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RuleRecyclerViewAdapter(this, rules);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // Start rules database and add this activity as observer.
        data = new RuleDataSource(getApplicationContext());
        data.addObserver(this);

        // On a click to the FAB open the rule activity to create a new rule.
        // bouton ajouter
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = NewRuleActivity.getStartActivityIntent(this);
            startActivity(intent);
        });*/
    }

    @Override
    public void onItemClick(int position) {
        // When an item in the recycler view is clicked, open the rule activity to edit the rule.
        /*
        on empêche la modification des rules quand on clique dessus
        Intent intent = EditRuleActivity.getStartActivityIntent(this, adapter.getItem(position).id);
        startActivity(intent);*/
        // on switch le enabled de la règle
        onItemSwitchClick(position);
    }

    @Override
    public void onItemSwitchClick(int position) {
        // When a switch of an item in the recycler view is clicked, toggle the rule enabled state.
        Rule rule = adapter.getItem(position);
        long ruleId = rule.id;

        if (ruleId == 100000 || ruleId == 100001) { // Si la règle est Insta Reels ou Insta Reels + from DM
            // Parcourir les règles pour trouver la règle Insta Reels ou Insta Reels + from DM
            for (Rule r : rules) {
                if ((r.id == 100000 || r.id == 100001) && r.id != ruleId) { // Si la règle est différente de la règle actuelle
                    // Désactiver la règle
                    r.enabled = false;
                    data.change(this, r); // Enregistrer le changement dans la base de données
                }
            }
        }

        // Activer ou désactiver la règle en fonction de l'état actuel
        Log.d("MainActivity", "Toggling rule " + rule.id + " to " + !rule.enabled);
        rule.enabled = !rule.enabled;
        data.change(this, rule);
    }


    @Override
    public void onStart() {
        super.onStart();





        // If the app doesn't have permission to bypass battery optimization, show a snackbar.
        if (!isIgnoringBatteryOptimizations()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            View container = findViewById(R.id.layoutContainer);
            Snackbar.make(container, R.string.battery_optimization_not_enabled_message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.battery_optimization_not_enabled_action,
                            v -> startActivity(intent))
                    .show();
        }


        // If a11y service is disabled show a snackbar with a button to take the user to
        // the setting that enables it.
        if (!A11yService.isServiceEnabled(getApplicationContext())) {
            Intent intent = A11yService.getOpenSettingIntent(getApplicationContext());
            View container = findViewById(R.id.layoutContainer);
            Snackbar.make(container, R.string.a11y_service_not_enabled_message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.a11y_service_not_enabled_action,
                            v -> startActivity(intent))
                    .show();
        }



        boolean ruleExistsInsta = false;
        for (Rule r : rules) {
            if (r.id == 100000) {
                ruleExistsInsta = true;
                break;
            }
        }
        if (!ruleExistsInsta) {
            Rule defaultRule = new Rule();
            defaultRule.id = 100000;
            defaultRule.name = "Instagram Reels (allow in DM)";
            defaultRule.enabled = false;
            defaultRule.appId = "com.instagram.android";
            defaultRule.viewId = "like_button";
            defaultRule.actionType = ActionType.ACTION_CLICK;
            data.add(this, defaultRule);
            runOnUiThread(adapter::notifyDataSetChanged);
        }

        boolean ruleExistsInstaBis = false;
        for (Rule r : rules) {
            if (r.id == 100001) {
                ruleExistsInstaBis = true;
                break;
            }
        }
        if (!ruleExistsInstaBis) {
            Rule defaultRule2 = new Rule();
            defaultRule2.id = 100001;
            defaultRule2.name = "Instagram Reels (everywhere)";
            defaultRule2.enabled = false;
            defaultRule2.appId = "com.instagram.android";
            defaultRule2.viewId = "like_button";
            defaultRule2.actionType = ActionType.ACTION_CLICK;
            data.add(this, defaultRule2);
            runOnUiThread(adapter::notifyDataSetChanged);
        }

        boolean ruleExistsYTB = false;
        for (Rule r : rules) {
            if (r.id == 100002) {
                ruleExistsYTB = true;
                break;
            }
        }
        if (!ruleExistsYTB) {
            Rule defaultRule3 = new Rule();
            defaultRule3.id = 100002;
            defaultRule3.name = "YouTube Shorts";
            defaultRule3.enabled = false;
            defaultRule3.appId = "com.google.android.youtube";
            defaultRule3.viewId = "reel_player_underlay";
            defaultRule3.actionType = ActionType.ACTION_CLICK;
            data.add(this, defaultRule3);
            runOnUiThread(adapter::notifyDataSetChanged);
        }

    }

    // Check if the app has permission to bypass battery optimization
    private boolean isIgnoringBatteryOptimizations() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return false;
        }
        return powerManager.isIgnoringBatteryOptimizations(getPackageName());
    }

    @Override
    public void onRuleLoad(List<Rule> rules) {
        this.rules.addAll(rules);
        runOnUiThread(adapter::notifyDataSetChanged);
    }

    @Override
    public void onRuleAdded(Rule rule) {
        rules.add(rule);
        runOnUiThread(adapter::notifyDataSetChanged);
    }

    @Override
    public void onRuleChanged(Rule rule) {
        rules.replaceAll(r -> r.id == rule.id ? rule : r);
        runOnUiThread(adapter::notifyDataSetChanged);
    }

    @Override
    public void onRuleRemoved(Rule rule) {
        rules.removeIf(r -> r.id == rule.id);
        runOnUiThread(adapter::notifyDataSetChanged);
    }

    @Override
    public void onRuleError(RuleDataSource.Error error, Rule rule) {
        System.out.println("onRuleError: " + error);
        System.out.format("Name: %s, Id: %d\n", rule.name, rule.id);
    }
}
