<?php

$langEn = file_get_contents('./res/values/strings.xml');
preg_match('/[0-9]{2}\.[0-9]{2}\.[0-9]{4}-[a-z]*/', $langEn, $build);

$serialFile = './src/com/helpers/DstabiProfile.java';
$content = file_get_contents($serialFile);

preg_match("/APLICATION_HELI_MAJOR_VERSION.*?(\\d+)/", $content, $majorHeli);
preg_match("/APLICATION_HELI_MINOR1_VERSION.*?(\\d+)/", $content, $minorHeli);


preg_match("/APLICATION_AERO_MAJOR_VERSION.*?(\\d+)/", $content, $majorAero);
preg_match("/APLICATION_AERO_MINOR1_VERSION.*?(\\d+)/", $content, $minorAero);

$helpinkFile = './src/com/helpers/HelpLinks.java';
$helpinkFileContent = file_get_contents($helpinkFile);

preg_match('/\/\/linkHeli.*\/\/endlinkHeli/s', $helpinkFileContent, $helpinkFileContentHeli);
preg_match('/\/\/linkAero.*\/\/endlinkAero/s', $helpinkFileContent, $helpinkFileContentAero);

$helpinkFileContentHeli = $helpinkFileContentHeli[0];
$helpinkFileContentAero = $helpinkFileContentAero[0];



$version = 'beta';
if(strpos($build[0], 'release')){
    $version = 'production';
}

preg_match_all("/\_(.*?)\./", $helpinkFileContentHeli, $helpLinkHeli);
preg_match_all("/\_(.*?)\./", $helpinkFileContentAero, $helpLinkAero);

$manifest = file_get_contents('AndroidManifest.xml');

$languages = "";

if(isset($helpLinkHeli[1]) && count($helpLinkHeli[1]) > 0){
    $languagesHeli = implode(',', $helpLinkHeli[1]);
}else{
    $languagesHeli = "!!!!!!!";
}

if(isset($helpLinkAero[1]) && count($helpLinkAero[1]) > 0){
    $languagesAero = implode(',', $helpLinkAero[1]);
}else{
    $languagesAero = "!!!!!!!";
}

preg_match("/android\:versionCode=\"([0-9]+)\"/", $manifest, $manifestVersion);
preg_match("/android\:versionName=\"(.*)\"/", $manifest, $manifestVersionName);


$settingsActivity = './src/com/spirit/SettingsActivity.java';
$settingsActivityContent = file_get_contents($settingsActivity);

echo "\n\n\n----------------------------------------\n";
echo "                   RESULT               \n";
echo "----------------------------------------\n";
echo "TYPE           : $version\n";
echo "VERSION HELI   : $majorHeli[1].$minorHeli[1]\n";
echo "VERSION AERO   : $majorAero[1].$minorAero[1]\n";
echo "BUILD          : $build[0]\n";
echo "HELP LINK HELI : [" . $languagesHeli . "]\n";
echo "HELP LINK AERO : [" . $languagesAero . "]\n";
echo "VER. CODE      : $manifestVersion[1]\n";
echo "VER. NAME      : $manifestVersionName[1]\n";
echo "----------------------------------------\n\n\n";