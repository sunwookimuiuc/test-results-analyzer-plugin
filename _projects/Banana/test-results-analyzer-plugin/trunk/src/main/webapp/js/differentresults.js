//differentresults.js


function getDiffs(resultData) {
    //empty list to append to
    var diffList = $j('<ul />');
    var packages = resultData['results'];
    var numBuilds = resultData['builds'].length;
    if (numBuilds < 2) {
        return diffList;
    }
        //iterate packages
        var numPackages = packages.length;
        for (var pkgIdx = 0; pkgIdx < numPackages; pkgIdx++) {
            //iterate classes
            var classes = packages[pkgIdx]['children'];
            var numClasses = classes.length;
            for (var clsIdx = 0; clsIdx < numClasses; clsIdx++) {
                //iterate tests (no action taken on iteration atm)
                var testCases = classes[clsIdx]['children'];
                var numTestCases = testCases.length;
                for (var tstIdx = 0; tstIdx < numTestCases; tstIdx++) {
                    //get results for current test case
                    var testCaseResults = testCases[tstIdx]['buildResults'];
                    //get last build results and second to last build results
                    //TODO: check if these exist first
                    var latestResult = testCaseResults[0];
                    var nextLatestResult = testCaseResults[1];
                    diffList = addDifferentResultsToList(diffList, latestResult, nextLatestResult);
                }
            
            }
        }
    return diffList;
}

function addDifferentResultsToList(diffList, newBuildResults, oldBuildResults) {
    var newStatus = newBuildResults['status'];
    var oldStatus = oldBuildResults['status'];
    if (newStatus != oldStatus) {
        var item = $j('<li />');
        var resultName = newBuildResults['name'];
        item.html('<span>' + resultName + ' changed from ' + oldStatus + ' to ' + newStatus + '</span>');
        diffList.append(item);
    }
    return diffList;
}

/*
//get last build results and second to last build results
//list differences
function getDiffs(latestBuildResults, secondToLatestBuildResults) {
    
}
getDiffs();
*/

