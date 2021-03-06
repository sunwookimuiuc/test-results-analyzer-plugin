/**
 * Utility class containing functions that generates a JSON object containing
 * data of test results from the ResultInfo object.
 */

package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.List;
import java.util.Set;

import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsTreeUtil {

    /**
     * Generates a JSON object containing data of test results from ResultInfo.
     * Filters out all test cases/suites that does not have a status specified
     * by statusFilter in any of the builds.
     * @method getJsTree
     * @param builds An array of build numbers
     * @param resultInfo The object containing data of all test results
     * @param statusFilter The status indicator for filtering
     * @return The JSONObject generated
     */
    public JSONObject getJsTree(List<Integer> builds, ResultInfo resultInfo, String statusFilter) {
        JSONObject tree = new JSONObject();

        JSONArray buildJson = new JSONArray();  // buildJson contains all the build numbers and renamed as build in the final tree
        for (Integer buildNumber : builds) {
            buildJson.add(buildNumber.toString());
        }
        tree.put("builds", buildJson);
        JSONObject packageResults = resultInfo.getJsonObject();
        JSONArray results = new JSONArray();
        for (Object packageName : packageResults.keySet()) { // this loop over all the packages, but there is only one package ?

            JSONObject packageJson = packageResults.getJSONObject((String) packageName);
            JSONObject subtree = createJson(builds, packageJson, statusFilter);
            if (subtree != null) {
                results.add(subtree);
            }
        }
        tree.put("results", results);
        return tree;
    }

    /**
     * Generates a JSON object containing statistics of the test result data.
     * @method getJsTreeCondensed
     * @param buildsCondense An array of condensed build numbers
     * @param builds An array of build numbers
     * @param resultInfo The object containing data of all test results
     * @param statusFilter The status indicator for filtering
     * @return The JSONObject generated
     */
    public JSONObject getJsTreeCondensed(List<Integer>buildsCondense,List<Integer> builds, ResultInfo resultInfo, String statusFilter) {
        // the condensed has only three component
        JSONObject tree = new JSONObject();
        JSONObject tree_new = new JSONObject();

        // buildJson contains all the build numbers and renamed as build in the final tree
        JSONArray buildJson = new JSONArray();
        JSONArray buildJson_new = new JSONArray();
        for (Integer buildNumber : builds) {
            buildJson.add(buildNumber.toString());
        }
        // Here it builds up the new base JSON Stuff, and using this base JSON it will build up the constructed JSON.
        buildJson_new.add("Passed");
        buildJson_new.add("Failed");
        buildJson_new.add("Skipped");

        // Put the JSON into the base tree.
        tree.put("builds", buildJson);
        tree_new.put("builds", buildJson_new);
        // the new tree has build json new as the builds

        // starting from here it gets the test-result and build up the JSON using the test results.
        JSONObject packageResults = resultInfo.getJsonObject();
        JSONArray results = new JSONArray();
        // this results_new should contain the condensed result
        JSONArray results_new = new JSONArray();
        // this loop over all the packages, but there is only one package
        for (Object packageName : packageResults.keySet()) {

            JSONObject packageJson = packageResults.getJSONObject((String) packageName);
            JSONObject subtree_new = createJsonCondensed(buildsCondense,builds, packageJson, statusFilter);
            if (subtree_new != null) {
                results_new.add(subtree_new);
            }
        }
        tree_new.put("results", results_new);

        //return tree;
        return tree_new;
    }


    private JSONObject getBaseJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", "");
        jsonObject.put("buildResults", new JSONArray());
        return jsonObject;
    }

    private JSONObject createJson(List<Integer> builds, JSONObject dataJson, String statusFilter) {
        JSONObject baseJson = getBaseJson();
        baseJson.put("text", dataJson.get("name"));
        baseJson.put("type", dataJson.get("type"));
        baseJson.put("buildStatuses", dataJson.get("buildStatuses"));
        JSONObject packageBuilds = dataJson.getJSONObject("builds");
        JSONArray treeDataJson = new JSONArray();
        boolean addToTree = false;
        if (statusFilter.equals("all")) {
            addToTree = true;
        }
        for (Integer buildNumber : builds) {
            JSONObject build = new JSONObject();
            if (packageBuilds.containsKey(buildNumber.toString())) {
                JSONObject buildResult = packageBuilds.getJSONObject(buildNumber.toString());
                buildResult.put("buildNumber", buildNumber.toString());
                build = buildResult;
                String status = buildResult.getString("status");
                if (status.equals(statusFilter)) {
                    addToTree = true;
                }
            } else {
                build.put("status", "N/A");
                build.put("buildNumber", buildNumber.toString());
            }
            treeDataJson.add(build);
        }
        if (!addToTree) {
            return null;
        }
        baseJson.put("buildResults", treeDataJson);

        if (dataJson.containsKey("children")) { // this part constructs the children
            JSONArray childrens = new JSONArray();
            JSONObject childrenJson = dataJson.getJSONObject("children"); // get the entire children set
            @SuppressWarnings("unchecked")
            Set<String> childeSet = (Set<String>) childrenJson.keySet(); // loop over the children set
            for (String childName : childeSet) {  // recursion to finish this one children. So when finished, this one children contains all of its children
                JSONObject subtree = createJson(builds, childrenJson.getJSONObject(childName), statusFilter);
                if (subtree != null) {
                    childrens.add(subtree); // add this one children to the tree
                }
            }
            baseJson.put("children", childrens); // when all the children are finished, put together the childrens
        }

        return baseJson;
    }

    private JSONObject createJsonCondensed(List<Integer>buildsCondense,List<Integer> builds, JSONObject dataJson, String statusFilter) {
        // this json has the results
        JSONObject baseJson = getBaseJson();
        JSONObject baseJson_new = getBaseJson();
        Integer num_pass=0;
        Integer num_fail=0;
        Integer num_skip=0;
        baseJson.put("text", dataJson.get("name"));
        baseJson.put("type", dataJson.get("type"));
        baseJson.put("buildStatuses", dataJson.get("buildStatuses"));

        baseJson_new.put("text", dataJson.get("name"));
        baseJson_new.put("type", dataJson.get("type"));
        baseJson_new.put("buildStatuses", dataJson.get("buildStatuses"));

        JSONObject packageBuilds = dataJson.getJSONObject("builds");
        JSONArray treeDataJson = new JSONArray();
        JSONArray treeDataJson_new = new JSONArray(); // this new tree data has only 3 build numbers
        boolean addToTree = false;
        if (statusFilter.equals("all")) {
            addToTree = true;
        }
        for (Integer buildNumber : builds) { // loop over all the builds
            JSONObject build = new JSONObject();
            if (packageBuilds.containsKey(buildNumber.toString())) {
                JSONObject buildResult = packageBuilds.getJSONObject(buildNumber.toString());
                buildResult.put("buildNumber", buildNumber.toString());
                build = buildResult;
                String status = buildResult.getString("status");
                if (status.equals(statusFilter)) {
                    addToTree = true;
                }
                String pass_str=build.getString("totalPassed");
                //String pass_str=build.getJSONObject("totalPassed").toString();
                num_pass=num_pass+Integer.parseInt(pass_str);
                //String fail_str=build.getJSONObject("totalFailed").toString();
                String fail_str=build.getString("totalFailed");
                num_fail=num_fail+Integer.parseInt(fail_str);
                //String skip_str=build.getJSONObject("totalSkipped").toString();
                String skip_str=build.getString("totalSkipped");
                num_skip=num_skip+Integer.parseInt(skip_str);
            } else {
                build.put("status", "N/A");
                build.put("buildNumber", buildNumber.toString());
            }
            treeDataJson.add(build); // add the subtree for each build
        }
        JSONObject build_pass = new JSONObject();
        JSONObject build_fail = new JSONObject();
        JSONObject build_skip = new JSONObject();
        build_pass.put("buildNumber", "pass");
        build_pass.put("status", num_pass.toString());
        build_fail.put("buildNumber", "fail");
        build_fail.put("status", num_fail.toString());
        build_skip.put("buildNumber", "skip");
        build_skip.put("status",num_skip.toString());
        treeDataJson_new.add(build_pass);
        treeDataJson_new.add(build_fail);
        treeDataJson_new.add(build_skip);

        if (!addToTree) {
            return null;
        }
        baseJson.put("buildResults", treeDataJson);
        baseJson_new.put("buildResults",treeDataJson_new);
        if (dataJson.containsKey("children")) { // this part constructs the children
            JSONArray childrens = new JSONArray();
            JSONArray childrens_new = new JSONArray();

            JSONObject childrenJson = dataJson.getJSONObject("children"); // get the entire children set
            @SuppressWarnings("unchecked")
            Set<String> childeSet = (Set<String>) childrenJson.keySet(); // loop over the children set
            for (String childName : childeSet) {  // recursion to finish this one children. So when finished, this one children contains all of its children
                JSONObject subtree = createJson(builds, childrenJson.getJSONObject(childName), statusFilter);
                JSONObject subtree_new = createJsonCondensed(buildsCondense,builds, childrenJson.getJSONObject(childName), statusFilter);
                if (subtree != null) {
                    childrens.add(subtree); // add this one children to the tree
                }
                if (subtree_new != null) {
                    childrens_new.add(subtree_new); // add this one children to the tree
                }
            }
            baseJson.put("children", childrens); // when all the children are finished, put together the childrens
            baseJson_new.put("children", childrens_new);
        }

        //return baseJson;
        return baseJson_new;
    }

}
