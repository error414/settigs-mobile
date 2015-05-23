<?php

$langEn = file_get_contents('./res/values/strings.xml');
preg_match('/[0-9]{2}\.[0-9]{2}\.[0-9]{4}-[a-z]*/', $langEn, $build);

$serialFile = './src/com/spirit/BaseActivity.java';
$content = file_get_contents($serialFile);

preg_match("/APLICATION_MAJOR_VERSION.*?(\\d+)/", $content, $major);
preg_match("/APLICATION_MINOR1_VERSION.*?(\\d+)/", $content, $minor);


$helpinkFile = './src/com/helpers/HelpLinks.java';
$helpinkFileContent = file_get_contents($helpinkFile);

$version = 'beta';
if(strpos($build[0], 'release')){
    $version = 'production';
}

preg_match_all("/\_(.*?)\./", $helpinkFileContent, $helpLink);

$manifest = file_get_contents('AndroidManifest.xml');

$languages = "";

if(isset($helpLink[1]) && count($helpLink[1]) > 0){
    $languages = implode(',', $helpLink[1]);
}else{
    $languages = "!!!!!!!";
}

preg_match("/android\:versionCode=\"([0-9]+)\"/", $manifest, $manifestVersion);
preg_match("/android\:versionName=\"(.*)\"/", $manifest, $manifestVersionName);


$settingsActivity = './src/com/spirit/SettingsActivity.java';
$settingsActivityContent = file_get_contents($settingsActivity);

if(strpos($settingsActivityContent, "/*com.lib.ChangeLog cl = new com.lib.ChangeLog(this);")){
    $donate = "DEACTIVATED";
}else{
    $donate = "ACTIVATED";
}


echo "\n\n\n----------------------------------------\n";
echo "                   RESULT               \n";
echo "----------------------------------------\n";
echo "TYPE      : $version\n";
echo "VERSION   : $major[1].$minor[1]\n";
echo "BUILD     : $build[0]\n";
echo "DONATE    : $donate\n";
echo "HELP LINK : [" . $languages . "]\n";
echo "VER. CODE : $manifestVersion[1]\n";
echo "VER. NAME : $manifestVersionName[1]\n";
echo "----------------------------------------\n\n\n";