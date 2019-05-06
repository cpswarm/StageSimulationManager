WARNING
=======

This directory contains JAR file dependencies that are intended ONLY FOR BUILD-TIME usage.
None are intended to be deployed as bundles into a running OSGi Framework, and indeed they may cause
unexpected errors if they are used at runtime.


-plugin.1.R7.API: \
	aQute.bnd.repository.maven.pom.provider.BndPomRepository; \
		snapshotUrls=https://oss.sonatype.org/content/repositories/osgi/; \
		releaseUrls=https://repo.maven.apache.org/maven2/; \
		revision=org.osgi.enroute:osgi-api:7.0.0; \
		readOnly=true; \
		name="OSGi R7 API"
-plugin.2.Enterprise.API: \
	aQute.bnd.repository.maven.pom.provider.BndPomRepository; \
		snapshotUrls=https://oss.sonatype.org/content/repositories/osgi/; \
		releaseUrls=https://repo.maven.apache.org/maven2/; \
		revision=org.osgi.enroute:enterprise-api:7.0.0; \
		readOnly=true; \
		name="Enterprise Java APIs"
-plugin.3.R7.Impl: \
	aQute.bnd.repository.maven.pom.provider.BndPomRepository; \
		snapshotUrls=https://oss.sonatype.org/content/repositories/osgi/; \
		releaseUrls=https://repo.maven.apache.org/maven2/; \
		revision=org.osgi.enroute:impl-index:7.0.0; \
		readOnly=true; \
		name="OSGi R7 Reference Implementations"
-plugin.4.Test: \
	aQute.bnd.repository.maven.pom.provider.BndPomRepository; \
		snapshotUrls=https://oss.sonatype.org/content/repositories/osgi/; \
		releaseUrls=https://repo.maven.apache.org/maven2/; \
		revision=org.osgi.enroute:test-bundles:7.0.0; \
		readOnly=true; \
		name="Testing Bundles"
-plugin.5.Debug: \
	aQute.bnd.repository.maven.pom.provider.BndPomRepository; \
		snapshotUrls=https://oss.sonatype.org/content/repositories/osgi/; \
		releaseUrls=https://repo.maven.apache.org/maven2/; \
		revision=org.osgi.enroute:debug-bundles:7.0.0; \
		readOnly=true; \
		name="Debug Bundles"

-plugin.7.Local: \
	aQute.bnd.deployer.repository.LocalIndexedRepo; \
		name = Local; \
		pretty = true; \
		local = ${build}/local

-plugin.8.Templates: \
	aQute.bnd.deployer.repository.LocalIndexedRepo; \
		name = Templates; \
		pretty = true; \
		local = ${build}/templates

-plugin.9.Release: \
	aQute.bnd.deployer.repository.LocalIndexedRepo; \
		name = Release; \
		pretty = true; \
		local = ${build}/release

-releaserepo: Release
-baselinerepo: Release
