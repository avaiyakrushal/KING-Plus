from pathlib import Path

BUILD = Path('app/build.gradle')
gradle = BUILD.read_text(encoding='utf-8')

if 'signingConfigs.releaseUpload' not in gradle:
    block = '''    signingConfigs {
        releaseUpload {
            def releaseStore = System.getenv('KINGPLUS_RELEASE_STORE_FILE')
            def releaseStorePassword = System.getenv('KINGPLUS_RELEASE_STORE_PASSWORD')
            def releaseAlias = System.getenv('KINGPLUS_RELEASE_KEY_ALIAS')
            def releaseKeyPassword = System.getenv('KINGPLUS_RELEASE_KEY_PASSWORD')
            if (!releaseStore || !releaseStorePassword || !releaseAlias || !releaseKeyPassword) {
                throw new GradleException('Production release signing environment variables are missing')
            }
            storeFile file(releaseStore)
            storePassword releaseStorePassword
            keyAlias releaseAlias
            keyPassword releaseKeyPassword
        }
    }

'''
    gradle = gradle.replace('    defaultConfig {', block + '    defaultConfig {', 1)

gradle = gradle.replace(
    '        release {\n            signingConfig signingConfigs.debug',
    '        release {\n            signingConfig signingConfigs.releaseUpload',
    1,
)

if 'signingConfig signingConfigs.releaseUpload' not in gradle:
    raise SystemExit('Could not switch release build to production signing config')

BUILD.write_text(gradle, encoding='utf-8')
print('Configured production upload-key signing for release build')
