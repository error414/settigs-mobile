<?php
function getTextInfoFormNewFile($md5, $newTsXml){
    foreach($newTsXml as $items){
        foreach($items->message as $message){
            $attr = $message->location;
            if((string)$attr['filename'] != '' && (md5($message->source) ==  $md5 || $md5 == md5($message->oldsource))) {
                return array($message->source, $message->translation, $md5 == md5($message->oldsource), md5($message->oldsource) . " -> " . md5($message->source));
            }
        }
    }
    return false;
}


function getTextInfoFormOldFile($md5, $oldTsXml){
    foreach($oldTsXml as $items){
        foreach($items->message as $message){
            $attr = $message->location;
            if((string)$attr['filename'] != '' && md5($message->source) ==  $md5) {
                return $message->source;
            }
        }
    }
    return false;
}


echo "BUILD help TEXT start\n";

$stringEnFile =  './res/values/strings.xml';
$stringCsFile =  './res/values-cs/strings.xml';
$helpMapCsFile = './src/com/helpers/HelpMap.java';
$tsFile = 		 './gradle-exec/lang_cs.ts';
$tsOldFile = 	 './gradle-exec/lang_cs_old.ts';


$helpMap = file_get_contents($helpMapCsFile);

preg_match_all('/st_([^\)]*)/', $helpMap, $res);

$res = $res[1];


$newTsXml = simplexml_load_file($tsFile);
$oldTsXml = simplexml_load_file($tsOldFile);


$stringEn = "<!-- HELP AUTO GENERATE STUB --> \n";
$stringCs = "<!-- HELP AUTO GENERATE STUB --> \n";
$needRewrite = "\n---------------------------------\n";
$needRewrite .= "NEED REWRITE\n";

$unknow= "\n---------------------------------\n";
$unknow .= "UNKNOW\n";

$i = 0;

foreach($res as $key => $oneRes){
    if($newTextInfo = getTextInfoFormNewFile($oneRes, $newTsXml)){
         $stringEn .= "    " . '<string name="st_' . md5($newTextInfo[0]) . '">' . str_replace(array("\n", '<br/>', '<br>', '%1'), array('\n', '\n', '\n', '°'), $newTextInfo[0]) . '</string>' . "\n";
         $stringCs .= "    " . '<string name="st_' . md5($newTextInfo[0]) . '">' . str_replace(array("\n", '<br/>', '<br>', '%1'), array('\n', '\n', '\n', '°'), $newTextInfo[1]) . '</string>' . "\n";
         if($newTextInfo[2]){
            $needRewrite .= $newTextInfo[3] . "\n";
         }

    }else{
        $unknow .= $oneRes . " : " . getTextInfoFormOldFile($oneRes, $oldTsXml) . "\n";
    }

    $i++;
}
$needRewrite .= "---------------------------------\n\n";
$unknow .= "---------------------------------\n\n";


//EN
$fileEn = file_get_contents($stringEnFile);
$stringEn .= "    <!--  END AUTO GENERATE STUB -->";
file_put_contents($stringEnFile, preg_replace('/\<\!\-\-.*\-\-\>/s', $stringEn, $fileEn));


//CS
$fileCs = file_get_contents($stringCsFile);
$stringCs .= "    <!--  END AUTO GENERATE STUB -->";

file_put_contents($stringCsFile, preg_replace('/\<\!\-\-.*\-\-\>/s', $stringCs, $fileCs));
echo "count: $i \n";
echo $needRewrite;
echo $unknow;
echo "BUILD help TEXT stop\n\n";
/* ---------------------------------------------------------------------------------------- */
/*
$tsFile = 		 'lang_cs.ts';

$xml = simplexml_load_file($tsFile);

foreach($xml as $items){
    foreach($items->message as $message){
            $attr = $message->location;
            echo '<hr>';
	        echo md5($message->source) . "<br>";

	        if((string)$attr['filename'] != '') {
		        echo '<b>' . $message->source . '</b>';
		        echo $message->oldsource;
	        }else{
	            echo $message->source;

	        }

            echo '<hr>';
    }
}
*/
