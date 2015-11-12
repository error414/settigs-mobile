<?php

echo "BUILD revision start\n";
date_default_timezone_set('Europe/Berlin');

$langEn = file_get_contents('./res/values/strings.xml');
$langCS = file_get_contents('./res/values-cs/strings.xml');

file_put_contents('./res/values/strings.xml',  preg_replace('/build: [0-9]{2}\.[0-9]{2}\.[0-9]{4}-[a-z]*/', 'build: ' .date('d.m.Y') . '-' . $mode, $langEn));
file_put_contents('./res/values-cs/strings.xml',  preg_replace('/sestavení: [0-9]{2}\.[0-9]{2}\.[0-9]{4}-[a-z]*/', 'sestavení: ' .date('d.m.Y') . '-' . $mode, $langCS));

echo 'build: ' .date('d.m.Y') . '-' . $mode . "\n";

echo "BUILD revision stop\n\n";