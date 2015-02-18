<?php
echo "Start BUILD help TEXT \n";

$stringEnFile =  './res/values/strings.xml';
$stringCsFile =  './res/values-cs/strings.xml';
$helpMapCsFile = './src/com/helpers/HelpMap.java';
$tsFile = 		 './gradle-exec/lang_cs.ts';



$helpMap = file_get_contents($helpMapCsFile);

preg_match_all('/st_([^\)]*)/', $helpMap, $res);

$res = $res[1];


$xml = simplexml_load_file($tsFile);

$stringEn = "<!-- HELP AUTO GENERATE STUB --> \n";
$stringCs = "<!-- HELP AUTO GENERATE STUB --> \n";

foreach($xml as $items){
    foreach($items->message as $message){
        $attr = $message->location;
        if(in_array(md5($message->source), $res) && (string)$attr['filename'] != '') {
            $stringEn .= "    " . '<string name="st_' . md5($message->source) . '">' . str_replace(array("\n", '<br/>', '<br>'), array('\n', '\n', '\n'), $message->source) . '</string>' . "\n";
            $stringCs .= "    " . '<string name="st_' . md5($message->source) . '">' . str_replace(array("\n", '<br/>', '<br>'), array('\n', '\n', '\n'), $message->translation) . '</string>' . "\n";
        }
    }
}



//EN
$fileEn = file_get_contents($stringEnFile);
$stringEn .= "    <!--  END AUTO GENERATE STUB -->";
file_put_contents($stringEnFile, preg_replace('/\<\!\-\-.*\-\-\>/s', $stringEn, $fileEn));


//CS
$fileCs = file_get_contents($stringCsFile);
$stringCs .= "    <!--  END AUTO GENERATE STUB -->";

file_put_contents($stringCsFile, preg_replace('/\<\!\-\-.*\-\-\>/s', $stringCs, $fileCs));

echo "Stop BUILD help TEXT";

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
	        }else{
	            echo $message->source;
	        }

            echo '<hr>';
    }
}
*/
