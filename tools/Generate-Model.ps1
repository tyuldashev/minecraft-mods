$ErrorActionPreference = 'Stop'
$root = Join-Path $PSScriptRoot '../src/main/resources/assets/villagerstaff'
'items','models/item','lang' | ForEach-Object { New-Item -ItemType Directory -Force (Join-Path $root $_) | Out-Null }
$script:parts = @()
function Box($from, $to, $texture) {
    $faces = @{}
    foreach ($face in @('north','south','east','west','up','down')) { $faces[$face] = @{texture="#$texture";uv=@(0,0,16,16)} }
    $script:parts += @{from=$from;to=$to;faces=$faces}
}
Box @(7.3,0,7.3) @(8.7,11,8.7) 'wood'
Box @(7,0,7) @(9,0.8,9) 'wood'
foreach ($y in @(2,6,10)) {
    Box @(7.1,$y,7.1) @(8.9,($y+0.55),8.9) 'gold'
    Box @(7.65,($y+0.1),6.95) @(8.35,($y+0.45),7.15) 'emerald'
}
# Square sun halo, with a stepped round orb inside.
Box @(4.8,10.6,7.5) @(5.35,16.2,8.5) 'gold'
Box @(10.65,10.6,7.5) @(11.2,16.2,8.5) 'gold'
Box @(4.8,10.6,7.5) @(11.2,11.15,8.5) 'gold'
Box @(4.8,15.65,7.5) @(11.2,16.2,8.5) 'gold'
Box @(7.55,16.2,7.6) @(8.45,17.1,8.4) 'gold'
Box @(3.9,12.95,7.6) @(4.8,13.85,8.4) 'gold'
Box @(11.2,12.95,7.6) @(12.1,13.85,8.4) 'gold'
Box @(6.4,12.3,6.8) @(9.6,14.5,9.2) 'light'
Box @(6.9,11.8,7.1) @(9.1,15,8.9) 'light'
Box @(6.9,12.7,6.5) @(9.1,14.1,9.5) 'light'
foreach ($p in @(@(4.8,13),@(10.65,13),@(7.7,15.65),@(7.7,10.6))) {
    Box @($p[0],$p[1],7.25) @(($p[0]+0.55),($p[1]+0.55),7.5) 'emerald'
}
$model = @{
    textures=@{wood='minecraft:block/oak_log';gold='minecraft:block/gold_block';emerald='minecraft:block/emerald_block';light='minecraft:block/yellow_concrete';particle='minecraft:block/gold_block'}
    elements=$script:parts
    display=@{
        thirdperson_righthand=@{rotation=@(0,-90,0);translation=@(0,3,1);scale=@(1,1,1)}
        thirdperson_lefthand=@{rotation=@(0,90,0);translation=@(0,3,1);scale=@(1,1,1)}
        firstperson_righthand=@{rotation=@(0,-90,12);translation=@(1,2,0);scale=@(0.8,0.8,0.8)}
        firstperson_lefthand=@{rotation=@(0,90,-12);translation=@(1,2,0);scale=@(0.8,0.8,0.8)}
        gui=@{rotation=@(0,0,-35);translation=@(0,-1,0);scale=@(0.8,0.8,0.8)}
        ground=@{rotation=@(0,0,0);translation=@(0,2,0);scale=@(0.5,0.5,0.5)}
        fixed=@{rotation=@(0,180,0);translation=@(0,0,0);scale=@(0.8,0.8,0.8)}
    }
}
$model | ConvertTo-Json -Depth 15 | Set-Content (Join-Path $root 'models/item/villager_staff.json')
@{model=@{type='minecraft:model';model='villagerstaff:item/villager_staff'}} | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $root 'items/villager_staff.json')
@{'item.villagerstaff.villager_staff'='Посох жителя';'message.villagerstaff.aim'='Наведите посох на блок в пределах 48 блоков'} | ConvertTo-Json | Set-Content (Join-Path $root 'lang/ru_ru.json')
@{'item.villagerstaff.villager_staff'='Villager Staff';'message.villagerstaff.aim'='Aim at a block within 48 blocks'} | ConvertTo-Json | Set-Content (Join-Path $root 'lang/en_us.json')
