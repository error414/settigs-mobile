<?php

$mode = isset($argv[1]) ? $argv[1] : '';

echo "PDF LINK CLASS start \n";

$serialFile = './src/com/helpers/DstabiProfile.java';
$helpinkFile = './src/com/helpers/HelpLinks.java';

$content = file_get_contents($serialFile);
$helpinkFileContent = file_get_contents($helpinkFile);


preg_match("/APLICATION_HELI_MAJOR_VERSION.*?(\\d+)/", $content, $major);
preg_match("/APLICATION_HELI_MINOR1_VERSION.*?(\\d+)/", $content, $minor);

$spiritLink = file_get_contents("http://spirit-system.com/index.php");

preg_match_all("/dl\/manual\/[^\"]*/", $spiritLink, $links);



$result = "//linkHeli\n";

foreach($links[0] as $res){
  preg_match('/\-([0-9]+)\.([0-9]+).*?([a-z]+)\.pdf/',$res ,$version);

  $majorWeb = $version[1];
  if($mode == 'devel'){
    $minorWeb = $version[2] + 1;
  }else{
    $minorWeb = $version[2];
  }

  $langWeb = $version[3];

  if($major[1] == $majorWeb && $minor[1] == $minorWeb){
    $result .= '            put("'.$langWeb.'", "'.$res.'");' . "\n";
  }
}

$result .= "            //endlinkHeli";

echo "--------------- \n";
echo str_replace(' ', '' ,$result) . "\n";
echo "--------------- \n";


file_put_contents($helpinkFile, preg_replace('/\/\/linkHeli.*\/\/endlinkHeli/s', $result, $helpinkFileContent));

echo "PDF LINK CLASS stop \n\n";
