$ErrorActionPreference='Stop'
$root=Join-Path $PSScriptRoot '../src/main/resources/assets/villagerstaff'
$script:parts=@()
function Box($from,$to,$texture){
 $faces=@{};foreach($f in @('north','south','east','west','up','down')){$faces[$f]=@{texture="#$texture";uv=@(0,0,16,16)}}
 $script:parts+=@{from=$from;to=$to;faces=$faces}
}
Box @(7.2,-3,7.2) @(8.8,3,8.8) 'black'
Box @(6.8,-3.5,6.8) @(9.2,-2,9.2) 'gold'
Box @(7.5,-3.1,6.6) @(8.5,-2.3,6.8) 'blue'
Box @(3,2.8,7) @(13,4,9) 'gold'
Box @(2.5,3,7) @(4,5,9) 'gold'
Box @(12,3,7) @(13.5,5,9) 'gold'
Box @(6.7,3,6.7) @(9.3,5,9.3) 'gold'
Box @(7.3,3.5,6.5) @(8.7,4.5,6.7) 'blue'
Box @(5.5,5,7.35) @(10.5,19,8.65) 'black'
Box @(5,5,7.4) @(5.5,19,8.6) 'gold'
Box @(10.5,5,7.4) @(11,19,8.6) 'gold'
Box @(6,19,7.35) @(10,20,8.65) 'black'
Box @(6,19,7.4) @(6.5,20,8.6) 'gold'
Box @(9.5,19,7.4) @(10,20,8.6) 'gold'
Box @(6.8,20,7.4) @(9.2,21,8.6) 'gold'
Box @(7.4,21,7.4) @(8.6,22,8.6) 'gold'
Box @(7.6,5,7.2) @(8.4,20.5,7.35) 'blue'
Box @(7.6,5,8.65) @(8.4,20.5,8.8) 'blue'
@{textures=@{black='minecraft:block/black_concrete';gold='minecraft:block/gold_block';blue='minecraft:block/light_blue_concrete';particle='minecraft:block/gold_block'};elements=$script:parts;display=@{
 gui=@{rotation=@(0,0,-35);translation=@(0,-1,0);scale=@(.58,.58,.58)}
 firstperson_righthand=@{rotation=@(0,-90,15);translation=@(1,2,0);scale=@(.7,.7,.7)}
 firstperson_lefthand=@{rotation=@(0,90,-15);translation=@(1,2,0);scale=@(.7,.7,.7)}
 thirdperson_righthand=@{rotation=@(0,-90,0);translation=@(0,4,1);scale=@(1,1,1)}
 thirdperson_lefthand=@{rotation=@(0,90,0);translation=@(0,4,1);scale=@(1,1,1)}
 ground=@{translation=@(0,3,0);scale=@(.4,.4,.4)}
 fixed=@{translation=@(0,0,0);scale=@(.6,.6,.6)}
}}|ConvertTo-Json -Depth 15|Set-Content "$root/models/item/kings_sword.json"
@{model=@{type='minecraft:model';model='villagerstaff:item/kings_sword'}}|ConvertTo-Json -Depth 5|Set-Content "$root/items/kings_sword.json"
foreach($lang in @('ru_ru','en_us')){
$p="$root/lang/$lang.json";$d=Get-Content $p -Raw|ConvertFrom-Json -AsHashtable
if($lang -eq 'ru_ru'){
$d['item.villagerstaff.kings_sword']='Меч короля';$d['effect.villagerstaff.kings_target']='Цель короля'
$d['message.villagerstaff.sword_cooldown']='Способность готова через %s сек.'
$d['message.villagerstaff.no_target']='Нет активной цели в этом измерении рядом с загруженными чанками'
$d['message.villagerstaff.no_safe_spot']='Рядом с целью нет свободного места для телепортации'
$d['message.villagerstaff.aim_target']='Наведитесь на противника в пределах 32 блоков'
$d['message.villagerstaff.mark_failed']='Не удалось наложить метку'
$d['message.villagerstaff.marked']='Цель короля: +20% урона на 40 секунд'
}else{
$d['item.villagerstaff.kings_sword']="King's Sword";$d['effect.villagerstaff.kings_target']="King's Target"
$d['message.villagerstaff.sword_cooldown']='Ability ready in %s seconds'
$d['message.villagerstaff.no_target']='No active loaded target in this dimension'
$d['message.villagerstaff.no_safe_spot']='No safe teleport location next to the target'
$d['message.villagerstaff.aim_target']='Aim at an enemy within 32 blocks'
$d['message.villagerstaff.mark_failed']='Could not apply the mark'
$d['message.villagerstaff.marked']="King's Target: +20% damage for 40 seconds"
};$d|ConvertTo-Json|Set-Content $p
}
