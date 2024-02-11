package fr.hlly.noreels.action;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.LinkedList;
import java.util.Queue;

import fr.hlly.noreels.service.RuleWithExtras;


/**
 * Action to click on the matched node.
 *
 * @author Niklaus Leuenberger
 */
public class ClickAction extends Action {
    private static final String TAG = "ClickAction";

    /**
     * How many levels in the parent chain will be searched for a clickable node.
     */
    private static final int FIND_CLICKABLE_RECURSION_LIMIT = 3;

    /**
     * Construct a new click action.
     *
     * @param service The accessibility service.
     */
    public ClickAction(AccessibilityService service) {
        super(service);
    }

    /**
     * Perform the action.
     *
     * @param node Node to execute the action on. It or a parent thereof will be clicked.
     */
    @Override
    public void triggerSeen(AccessibilityNodeInfo node, RuleWithExtras ruleWithExtras) {
        Log.d(TAG, "Executing click action");

        long ruleId = ruleWithExtras.r.id;
        AccessibilityNodeInfo feedTabNode = null;

        if (ruleId == 100000) { // Instagram Reels
            feedTabNode = findNodeWithId(node, "feed_tab");
            if (feedTabNode == null) {
                Log.e(TAG, "No node with view id 'feed_tab' found");
                return;
            }
        } else if (ruleId == 100001) { // Instagram Reels + via MP
            feedTabNode = findNodeWithId(node, "action_bar_button_back");
            if (feedTabNode == null) {
                Log.e(TAG, "No node with view id 'action_bar_button_back' found");
                return;
            }
        } else if (ruleId == 100002) { // YTB
            feedTabNode = findNodeWithId(node, "pivot_bar");
            if (feedTabNode == null) {
                Log.e(TAG, "No node with view id YTB found");
                return;
            }
        }

        if (feedTabNode != null) {
            feedTabNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }




    private AccessibilityNodeInfo findNodeWithId(AccessibilityNodeInfo rootNode, String viewId) {
        if (rootNode == null) {
            return null;
        }

        AccessibilityNodeInfo currentParent = rootNode;
        AccessibilityNodeInfo feedTabNode = null;

        // Parcourir les parents jusqu'à la racine de la vue
        while (currentParent != null) {
            // Recherche de l'élément feed_tab dans le parent actuel
            feedTabNode = findClickableNodeWithId(currentParent, viewId);
            if (feedTabNode != null) {
                Log.d(TAG, "Found clickable node");
                return feedTabNode;
            }
            // Passer au parent suivant
            currentParent = currentParent.getParent();
        }

        Log.e(TAG, "No node with view id found in the hierarchy");
        return null;
    }


    private AccessibilityNodeInfo findClickableNodeWithId(AccessibilityNodeInfo rootNode, String viewId) {
        if (rootNode == null) {
            return null;
        }

        Queue<AccessibilityNodeInfo> queue = new LinkedList<>();
        queue.add(rootNode);

        while (!queue.isEmpty()) {
            AccessibilityNodeInfo currentNode = queue.poll();

            // Vérifie si la chaîne contient l'identifiant de vue recherché
            String nodeViewId = currentNode.getViewIdResourceName();
            CharSequence nodeText = currentNode.getText();
            if (nodeText != null) {
                //Log.d(TAG, nodeText.toString());
            }

            // cas pour instagram
            if (nodeViewId != null && nodeViewId.contains(viewId) && currentNode.isClickable() && viewId != "pivot_bar") {
                return currentNode;

                // cas pour YTB
            } else if (nodeViewId != null && nodeViewId.contains(viewId)) {
                AccessibilityNodeInfo firstChild = getChildNode(currentNode);
                if (firstChild != null) {
                    return firstChild;
                }
            }

            int childCount = currentNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo childNode = currentNode.getChild(i);
                if (childNode != null) {
                    queue.add(childNode);
                }
            }
        }

        return null;
    }


    // pour YTB c'est plus complexe, sachant qu'il n'y a pas d'id il faut naviguer dans les childs du bottom nav
    private AccessibilityNodeInfo getChildNode(AccessibilityNodeInfo node) {
        if (node == null || node.getChildCount() == 0) {
            return null;
        }
        AccessibilityNodeInfo firstChild = node.getChild(0);
        if (firstChild != null && firstChild.getChildCount() > 0) {
            return firstChild.getChild(0);
        }
        return null;
    }



    /**
     * Trigger on gone missing of node. Do nothing.
     */
    @Override
    public void triggerGone() {
        // Do nothing.
    }

    /**
     * The triggering node may not be clickable. This method will find the first clickable node
     * along the parent chain.
     *
     * @param node           The node to start from.
     * @param recursionLevel The current recursion level.
     * @return The first clickable node or null if max recursion level was reached.
     */
    private AccessibilityNodeInfo findClickableNode(AccessibilityNodeInfo node, int recursionLevel) {
        if (node.isClickable()) {
            Log.d(TAG, "Found clickable node " + node.getViewIdResourceName() + " at recursion level " + recursionLevel);
            return node;
        }
        if (recursionLevel > FIND_CLICKABLE_RECURSION_LIMIT) {
            Log.e(TAG, "Reached max recursion level while searching clickable node.");
            return null;
        }
        AccessibilityNodeInfo parent = node.getParent();
        if (parent == null) {
            return null;
        }
        return findClickableNode(parent, recursionLevel + 1);
    }
}
