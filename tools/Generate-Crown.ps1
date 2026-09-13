$ErrorActionPreference='Stop'
$root=Join-Path $PSScriptRoot '../src/main/resources/assets/villagerstaff'
$script:parts=@()
function Box($from,$to,$texture){
 $faces=@{}; foreach($f in @('north','south','east','west','up','down')){$faces[$f]=@{texture="#$texture";uv=@(0,0,16,16)}}
 $script:parts+=@{from=$from;to=$to;faces=$faces}
}
Box @(0,12,0) @(16,15,1.5) 'gold'
Box @(0,12,14.5) @(16,15,16) 'gold'
Box @(0,12,1.5) @(1.5,15,14.5) 'gold'
Box @(14.5,12,1.5) @(16,15,14.5) 'gold'
foreach($x in @(0,6.5,13)){
 foreach($z in @(0,14.5)){
  Box @($x,15,$z) @(($x+3),17,($z+1.5)) 'gold'
  Box @(($x+0.75),17,$z) @(($x+2.25),19,($z+1.5)) 'gold'
 }
}
foreach($x in @(0,14.5)){
 Box @($x,15,6.5) @(($x+1.5),17,9.5) 'gold'
 Box @($x,17,7.25) @(($x+1.5),19,8.75) 'gold'
}
Box @(6.75,12.5,-0.3) @(9.25,14.5,0.2) 'ruby'
Box @(6.75,12.5,15.8) @(9.25,14.5,16.3) 'ruby'
$model=@{textures=@{gold='minecraft:block/gold_block';ruby='minecraft:block/redstone_block';particle='minecraft:block/gold_block'};elements=$script:parts;display=@{
 head=@{rotation=@(0,0,0);translation=@(0,0,0);scale=@(1,1,1)}
 gui=@{rotation=@(25,-35,0);translation=@(0,-5,0);scale=@(0.65,0.65,0.65)}
 ground=@{translation=@(0,-2,0);scale=@(0.4,0.4,0.4)}
 fixed=@{translation=@(0,-5,0);scale=@(0.6,0.6,0.6)}
 firstperson_righthand=@{rotation=@(0,-35,0);translation=@(0,-3,0);scale=@(0.5,0.5,0.5)}
 firstperson_lefthand=@{rotation=@(0,35,0);translation=@(0,-3,0);scale=@(0.5,0.5,0.5)}
 thirdperson_righthand=@{translation=@(0,-3,0);scale=@(0.5,0.5,0.5)}
 thirdperson_lefthand=@{translation=@(0,-3,0);scale=@(0.5,0.5,0.5)}
}}
$model | ConvertTo-Json -Depth 15 | Set-Content "$root/models/item/kings_crown.json"
@{model=@{type='minecraft:model';model='villagerstaff:item/kings_crown'}}|ConvertTo-Json -Depth 5|Set-Content "$root/items/kings_crown.json"
foreach($lang in @('ru_ru','en_us')){
 $p="$root/lang/$lang.json";$data=Get-Content $p -Raw|ConvertFrom-Json -AsHashtable
 $data['item.villagerstaff.kings_crown']=if($lang -eq 'ru_ru'){'Корона короля'}else{"King's Crown"}
 $data|ConvertTo-Json|Set-Content $p
}
