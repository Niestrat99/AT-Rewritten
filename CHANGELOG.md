# Changelog
All notable changes to this project will be documented in this file. See [conventional commits](https://www.conventionalcommits.org/) for commit guidelines.

- - -
## [v6.1.1](https://github.com/Niestrat99/AT-Rewritten/compare/v6.1.0..v6.1.1) - 2024-03-11

- - -

## [v6.1.0](https://github.com/Niestrat99/AT-Rewritten/compare/v6.0.0-rc.5..v6.1.0) - 2024-03-11
#### Bug Fixes
- **(README.md)** lmao discord doesn't have permanent storage anymore - ([a775558](https://github.com/Niestrat99/AT-Rewritten/commit/a775558ff238d6dfcb708f4ddcac3bc317f54ddf)) - Holly P
- I did a stupid a few commits ago - ([c62fc35](https://github.com/Niestrat99/AT-Rewritten/commit/c62fc352970feff44cf81da185f09bcd65b1fb5f)) - Thatsmusic99
- teleport requests not working with multiple requests disabled - ([59a28ed](https://github.com/Niestrat99/AT-Rewritten/commit/59a28ed9e464528f2c508799d7935ef93f7e16e8)) - Thatsmusic99
- yeah, you mind removing searching players if they fail? - ([1c91cfd](https://github.com/Niestrat99/AT-Rewritten/commit/1c91cfd546de995065e711fd6bc4b863c3e8b5e5)) - Thatsmusic99
- interactive messages on Spigot not working - ([2ff3cd6](https://github.com/Niestrat99/AT-Rewritten/commit/2ff3cd60ff4d7f0eff4e70e507d7fc19e68751f2)) - Thatsmusic99
- home deletion in SQL running on the main thread!? - ([4699094](https://github.com/Niestrat99/AT-Rewritten/commit/4699094033b1ec9922b79adb505ac955dacafbcb)) - Thatsmusic99
- check for errors in async /home and /homes - ([eaf57f9](https://github.com/Niestrat99/AT-Rewritten/commit/eaf57f945e0d6829bbff002bd8093b25376c1a20)) - Thatsmusic99
- backtick key to fix database errors - ([f39f73c](https://github.com/Niestrat99/AT-Rewritten/commit/f39f73c73e0c0bf7787820d2e80af1935196ed81)) - Thatsmusic99
- formatting error in config.yml - ([392833d](https://github.com/Niestrat99/AT-Rewritten/commit/392833da6a85f0ff41e976ff6290e428af8f9d0d)) - Thatsmusic99
- compilation error - ([563d03d](https://github.com/Niestrat99/AT-Rewritten/commit/563d03deb4eff8fa159ac618818ba5b176aaf359)) - Thatsmusic99
#### Miscellaneous Chores
- **(deps)** update floodgate to 2.2.2-SNAPSHOT - ([bfb1c42](https://github.com/Niestrat99/AT-Rewritten/commit/bfb1c425febd51386764c2d6757ccdef44de5018)) - Thatsmusic99
- **(deps)** update adventure to 4.15.0 - ([9fdc495](https://github.com/Niestrat99/AT-Rewritten/commit/9fdc49588dd131f4b50d099fb329ef5ea7a353af)) - Thatsmusic99
- **(deps)** update Geyser repo - ([b010695](https://github.com/Niestrat99/AT-Rewritten/commit/b010695f1ca2706efe91359da2902fe17969cd3e)) - Thatsmusic99
- **(deps)** bump ConfigurationMaster to v2.0.0-rc.2 - ([9deca34](https://github.com/Niestrat99/AT-Rewritten/commit/9deca346cc9c7f7a6cdc5f24814d102d2b2dfdb8)) - Thatsmusic99
- **(version)** bump to 6.0.0 (yippee!!) - ([a54ce35](https://github.com/Niestrat99/AT-Rewritten/commit/a54ce35d8b2d54a3a65771f302f16669be1c7237)) - Thatsmusic99
- removed urgent flag in invalid block checking + extra debugging - ([3ae9fb7](https://github.com/Niestrat99/AT-Rewritten/commit/3ae9fb7a3f146d437861275ee65753d56ed62731)) - Thatsmusic99
- clean up code - ([19ed82a](https://github.com/Niestrat99/AT-Rewritten/commit/19ed82a262322815cdcded3919a87bdc44ab3adc)) - Thatsmusic99

- - -

## [v6.0.0-beta.5](https://github.com/Niestrat99/AT-Rewritten/compare/v6.0.0-beta.4..v6.0.0-beta.5) - 2023-06-06
#### Bug Fixes
- legacy codes not being translated in messages - ([e8a1b31](https://github.com/Niestrat99/AT-Rewritten/commit/e8a1b31c4a42403b11eaea8e0c37d3174e545127)) - Thatsmusic99
- at.member.core not considered a default member permission - ([b44036a](https://github.com/Niestrat99/AT-Rewritten/commit/b44036ab93926551605575db9126823421d38e5d)) - Thatsmusic99
- use current location rather than previous location when respawning - ([94be286](https://github.com/Niestrat99/AT-Rewritten/commit/94be2869108cdad96159afd8273184439bbfc377)) - Thatsmusic99
- set respawn priority to HIGHEST, *hopefully* beating down other plugins - ([ec0efcb](https://github.com/Niestrat99/AT-Rewritten/commit/ec0efcb6f09b429a6e482ea75c60a9951cf97b5e)) - Thatsmusic99
- change admin core permissions to at.admin.core.command - ([334d8b4](https://github.com/Niestrat99/AT-Rewritten/commit/334d8b4417ab9ee14274fb07e492e47c6c8d868d)) - Thatsmusic99
- make info section lenient - ([853a915](https://github.com/Niestrat99/AT-Rewritten/commit/853a91507a47c43b77ab19fddf834a5cfff6ad46)) - Thatsmusic99
- NPE without action bars - ([fd2dc55](https://github.com/Niestrat99/AT-Rewritten/commit/fd2dc55fba3f1c264981335ba297c9dcdfaa3b5e)) - Thatsmusic99
#### Features
- add /atp and /adtp as aliases - ([584980a](https://github.com/Niestrat99/AT-Rewritten/commit/584980a6a7f400ff43dafcbdf8c1fe4f905ef158)) - Thatsmusic99
- add teleportation to the nearest spawnpoint - ([6bda7cf](https://github.com/Niestrat99/AT-Rewritten/commit/6bda7cf676aef1cf1727217724cc60c811ed64ad)) - Thatsmusic99
- allow blank messages to be replaced with actionbar messages in the console - ([62e1f34](https://github.com/Niestrat99/AT-Rewritten/commit/62e1f34cf2aaaecf75f4cc9699bd718521232a91)) - Thatsmusic99
- add sounds to messages - ([206e41d](https://github.com/Niestrat99/AT-Rewritten/commit/206e41d4d9e2d1d40bc17528b8e4014814f7551d)) - Thatsmusic99
- add action bar and improve title support - ([14992b4](https://github.com/Niestrat99/AT-Rewritten/commit/14992b475829bf58ffce0ed3e738d8b51a03999c)) - Thatsmusic99
#### Miscellaneous Chores
- **(version)** bump - ([93f0250](https://github.com/Niestrat99/AT-Rewritten/commit/93f025025b6fcf295e8866cd4fe7d68c17d3b558)) - Thatsmusic99
- remove target folder - ([65c54c8](https://github.com/Niestrat99/AT-Rewritten/commit/65c54c868701e075dd32bf625412aada7ca5ac95)) - Thatsmusic99

- - -

## [v6.0.0-beta.2](https://github.com/Niestrat99/AT-Rewritten/compare/v6.0.0-beta.1..v6.0.0-beta.2) - 2023-04-11
#### Bug Fixes
- change repo owner to Niestrat99 - ([4eca7a2](https://github.com/Niestrat99/AT-Rewritten/commit/4eca7a284e5c5de98f1ed9ba545d39d34a9c1129)) - Thatsmusic99
- u wanna work? - ([6333a89](https://github.com/Niestrat99/AT-Rewritten/commit/6333a89d75627b3126679728feb1b04b0cda2d93)) - Thatsmusic99
- Weak player referencing (#102) - ([bace4d2](https://github.com/Niestrat99/AT-Rewritten/commit/bace4d2647728451a6febf77ead4a23ea065f8f5)) - TM (Holly)
#### Development/Build Changes
- remove pre-bump hooks temporarily - ([ec3081c](https://github.com/Niestrat99/AT-Rewritten/commit/ec3081cd70fcff02ca88dfd2bdc28c1806048e58)) - Thatsmusic99
- apparently not - ([300bbb5](https://github.com/Niestrat99/AT-Rewritten/commit/300bbb54841ffb6b1d1c1c83ac3a8fb5dd59edfe)) - Thatsmusic99
- make internal tweaks - ([34e7d52](https://github.com/Niestrat99/AT-Rewritten/commit/34e7d5258e8ea36221f78baa86eeed4ae795d9b6)) - Thatsmusic99
- remove changelog task - ([ea71504](https://github.com/Niestrat99/AT-Rewritten/commit/ea71504361b85eaa1701262b7610bd69ba950de4)) - Thatsmusic99
- add codeql to github repo (#103) - ([da0950b](https://github.com/Niestrat99/AT-Rewritten/commit/da0950bb1c830355e037e0e50670f28db0bad830)) - TM (Holly)

- - -

## [v6.0.0-alpha.1](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.9..v6.0.0-alpha.1) - 2022-12-27
#### Bug Fixes
- wrong directory being ignored - ([76d8243](https://github.com/Thatsmusic99/AT-Rewritten/commit/76d8243641790d5821be0d9f1b32399617f58b97)) - Thatsmusic99
- re-added some lost content - ([acd6084](https://github.com/Thatsmusic99/AT-Rewritten/commit/acd60849bd553144b29e6720c1c1577389739bb4)) - Thatsmusic99
#### Development/Build Changes
- Cocogitto and Minotaur Integration (#100) - ([ccf3560](https://github.com/Thatsmusic99/AT-Rewritten/commit/ccf35605e19ba644ea64c461be74383a24746e6f)) - TM (Holly)
- Cocogitto and Minotaur Integration (#100) - ([12f5b56](https://github.com/Thatsmusic99/AT-Rewritten/commit/12f5b56a031759818e9a0477b08b5972f76e8129)) - TM (Holly)
#### Miscellaneous Chores
- remove and ignore META-INF - ([6ac60d4](https://github.com/Thatsmusic99/AT-Rewritten/commit/6ac60d4964694fad72deab690b56f00f3ec59c58)) - Thatsmusic99
- un-conflict - ([9cc54bc](https://github.com/Thatsmusic99/AT-Rewritten/commit/9cc54bc765c8c2e7362ae58e5ff367369de0a03e)) - Thatsmusic99

- - -

## [v6.0.0-alpha.9](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.8..v6.0.0-alpha.9) - 2022-12-26
#### Features
- add Modrinth changelog - ([a00d013](https://github.com/Thatsmusic99/AT-Rewritten/commit/a00d01379b110c9ed8810f3a97b290ba0e1af1a3)) - Thatsmusic99

- - -

## [v6.0.0-alpha.8](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.7..v6.0.0-alpha.8) - 2022-12-26
#### Bug Fixes
- correct shenanigans and avoid two commits - ([7b4206b](https://github.com/Thatsmusic99/AT-Rewritten/commit/7b4206b78f6c26a31519215c90fa9800f34b1d02)) - Thatsmusic99
- compilation errors - ([764ab1a](https://github.com/Thatsmusic99/AT-Rewritten/commit/764ab1a18d878783eed7650a315e3434c30bd791)) - Thatsmusic99
- resolve half-finished dependency management - ([18776a6](https://github.com/Thatsmusic99/AT-Rewritten/commit/18776a600704e3083d197f30fda3d4054d608530)) - Thatsmusic99
#### Build system
- Update version to vv6.0.0-alpha.8 - ([dccf53e](https://github.com/Thatsmusic99/AT-Rewritten/commit/dccf53ee9d527c0edf4aa4cc80320e0ca1ef62f9)) - Thatsmusic99
- Update version to v6.0.0-alpha.7 - ([eee05d0](https://github.com/Thatsmusic99/AT-Rewritten/commit/eee05d04f279afce35c48650bdc61261d1a93a5f)) - Thatsmusic99
#### Features
- whoops pre submit script lol - ([f8e06b1](https://github.com/Thatsmusic99/AT-Rewritten/commit/f8e06b185420f6268fe8960f9d28b4429c310aa4)) - Thatsmusic99
#### Miscellaneous Chores
- remove unneeded files - ([b98bcc9](https://github.com/Thatsmusic99/AT-Rewritten/commit/b98bcc9ec5763b356cac03be339c85f85b0faa06)) - Thatsmusic99

- - -

## [v6.0.0-alpha.7](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.6..v6.0.0-alpha.7) - 2022-12-26
#### Build system
- Update version to v6.0.0-alpha.6 - ([d502610](https://github.com/Thatsmusic99/AT-Rewritten/commit/d50261076ea3c49009f69b166a0cb3f3f73ff826)) - Thatsmusic99
#### Development/Build Changes
- add minotaur run - ([101b2e9](https://github.com/Thatsmusic99/AT-Rewritten/commit/101b2e98d8f5d0ff136f85b196ad29854edbc00e)) - Thatsmusic99

- - -

## [v6.0.0-alpha.6](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.5..v6.0.0-alpha.6) - 2022-12-26
#### Build system
- Update version to v6.0.0-alpha.5 - ([817f026](https://github.com/Thatsmusic99/AT-Rewritten/commit/817f0260e0b07c3a629875bc452c91cd74661238)) - Thatsmusic99
#### Development/Build Changes
- good god - ([68c0d47](https://github.com/Thatsmusic99/AT-Rewritten/commit/68c0d47750bc3e18ef965a942a26c9c026c06a3f)) - Thatsmusic99

- - -

## [v6.0.0-alpha.5](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.3..v6.0.0-alpha.5) - 2022-12-26
#### Build system
- Update version to v - ([60e86eb](https://github.com/Thatsmusic99/AT-Rewritten/commit/60e86ebcd10fa8e4cc7f970fae16bc335841a513)) - Thatsmusic99
#### Development/Build Changes
- update dev script AGAIN - ([8b06102](https://github.com/Thatsmusic99/AT-Rewritten/commit/8b061024c5b9be22806c501ebf79ca0603647518)) - Thatsmusic99

- - -

## [v6.0.0-alpha.3](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.2..v6.0.0-alpha.3) - 2022-12-26
#### Development/Build Changes
- Update post-bump script again - ([05f04e0](https://github.com/Thatsmusic99/AT-Rewritten/commit/05f04e065e1a36f433847535ba999f5ee1adaecc)) - Thatsmusic99
- Update post-bump script - ([a9388c2](https://github.com/Thatsmusic99/AT-Rewritten/commit/a9388c2eb32704a8a1f9c1df3d05f5f2a0b24d78)) - Thatsmusic99
#### Miscellaneous Chores
- **(version)** v6.0.0-alpha.2 - ([08db975](https://github.com/Thatsmusic99/AT-Rewritten/commit/08db9750cea51d0c4cfc12fa8cc71819ea78d922)) - Thatsmusic99
- **(version)** v6.0.0-alpha.2 - ([63f2ed5](https://github.com/Thatsmusic99/AT-Rewritten/commit/63f2ed5e2ca8338dc4a63b9f3347de57a1d7ed50)) - Thatsmusic99

- - -

## [v6.0.0-alpha.2](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.2..v6.0.0-alpha.2) - 2022-12-26
#### Development/Build Changes
- Update post-bump script again - ([05f04e0](https://github.com/Thatsmusic99/AT-Rewritten/commit/05f04e065e1a36f433847535ba999f5ee1adaecc)) - Thatsmusic99
- Update post-bump script - ([a9388c2](https://github.com/Thatsmusic99/AT-Rewritten/commit/a9388c2eb32704a8a1f9c1df3d05f5f2a0b24d78)) - Thatsmusic99
#### Miscellaneous Chores
- **(version)** v6.0.0-alpha.2 - ([63f2ed5](https://github.com/Thatsmusic99/AT-Rewritten/commit/63f2ed5e2ca8338dc4a63b9f3347de57a1d7ed50)) - Thatsmusic99

- - -

## [v6.0.0-alpha.2](https://github.com/Thatsmusic99/AT-Rewritten/compare/v6.0.0-alpha.2..v6.0.0-alpha.2) - 2022-12-26
#### Development/Build Changes
- Update post-bump script - ([a9388c2](https://github.com/Thatsmusic99/AT-Rewritten/commit/a9388c2eb32704a8a1f9c1df3d05f5f2a0b24d78)) - Thatsmusic99

- - -

## [v6.0.0-alpha.2](https://github.com/Thatsmusic99/AT-Rewritten/compare/v5.4.6-SNAPSHOT-3..v6.0.0-alpha.2) - 2022-12-26
#### Development/Build Changes
- add post-bump script - ([3c3d4bc](https://github.com/Thatsmusic99/AT-Rewritten/commit/3c3d4bcccc7cdf4261d06e11d90c62a55c67a87b)) - Thatsmusic99
- Add Minotaur and Cocogitto (internal only) support - ([8dd4b88](https://github.com/Thatsmusic99/AT-Rewritten/commit/8dd4b8826d883ec60a33e7c88eadb5ab5b40e6bc)) - Thatsmusic99
#### Miscellaneous Chores
- change to separate elements properly - ([ad70141](https://github.com/Thatsmusic99/AT-Rewritten/commit/ad701411f530054388527c8f121da07b2a60d7cc)) - Thatsmusic99

- - -

Changelog generated by [cocogitto](https://github.com/cocogitto/cocogitto).