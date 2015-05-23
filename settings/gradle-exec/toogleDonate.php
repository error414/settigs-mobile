<?php

$revision = isset($argv[1]) ? $argv[1] : '';

echo "DONATE start \n";

$settingsActivity = './src/com/spirit/SettingsActivity.java';

$content = file_get_contents($settingsActivity);


$donateProduction = "//AUTO GENERATE DONATE START
		com.lib.ChangeLog cl = new com.lib.ChangeLog(this);
	    if (cl.firstRun()){
	        cl.getLogDialog().show();
	    }
        //AUTO GENERATE DONATE END";

$donateDevel = "//AUTO GENERATE DONATE START
        /*com.lib.ChangeLog cl = new com.lib.ChangeLog(this);
        	    if (cl.firstRun()){
        	        cl.getLogDialog().show();
        	    }*/
        //AUTO GENERATE DONATE END";

if($revision == 'devel'){
   $content = preg_replace("/\/\/AUTO GENERATE DONATE START.*\/\/AUTO GENERATE DONATE END/s", $donateDevel, $content);
   echo "DONATE DEACTIVATED\n";
}else{
   $content = preg_replace("/\/\/AUTO GENERATE DONATE START.*\/\/AUTO GENERATE DONATE END/s", $donateProduction, $content);
   echo "DONATE ACTIVATED\n";
}


file_put_contents($settingsActivity, $content);

echo "DONATE stop \n\n";
