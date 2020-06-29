<xsl:stylesheet version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/">
	<h2>Component Dictionary</h2>
	<h3>Flow Services</h3>
	<ol>
		<xsl:for-each select="Dictionary/Flows/Flow">
		<h4><li><xsl:value-of select="Name"/></li></h4>
		<table>
		<tr><td><b>Full Name</b></td><td><xsl:value-of select="FullName"/></td></tr>
		<tr><td><b>Type</b></td><td><xsl:value-of select="Type"/></td></tr>
		<tr><td><b>Package</b></td><td><xsl:value-of select="Package"/></td></tr>
		<tr><td><b>File Path</b></td><td><xsl:value-of select="FilePath"/></td></tr>

		<tr><td colspan="2"><b>Internal Dependencies</b></td></tr>
		<xsl:for-each select="InternalDependencies">
		<tr><td></td><td><xsl:value-of select="Dependency"/></td></tr>
		</xsl:for-each>

		<tr><td colspan="2"><b>External Service Dependencies</b></td></tr>
		<xsl:for-each select="ExternalServiceDependencies">
		<tr><td></td><td><xsl:value-of select="Dependency"/></td></tr>
		</xsl:for-each>

		<tr><td colspan="2"><b>Internal Service Dependencies</b></td></tr>
		<xsl:for-each select="InternalServiceDependencies">
		<tr><td></td><td><xsl:value-of select="Dependency"/></td></tr>
		</xsl:for-each>

		<tr><td colspan="2"><b>Properties</b></td></tr>
		<xsl:for-each select="Properties/Property">
		<tr><td><xsl:value-of select="Name"/></td><td><xsl:value-of select="Value"/></td></tr>
		</xsl:for-each>

		</table>		
		</xsl:for-each>
	</ol>
	<h3>Components</h3>
	<ol>
		<xsl:for-each select="Dictionary/Nodes/Node">
		<h4><li><xsl:value-of select="Name"/></li></h4>
		<table>
		<tr><td><b>Full Name</b></td><td><xsl:value-of select="FullName"/></td></tr>
		<tr><td><b>Type</b></td><td><xsl:value-of select="Type"/></td></tr>
		<tr><td><b>Sub Type</b></td><td><xsl:value-of select="SubType"/></td></tr>
		<tr><td><b>Package</b></td><td><xsl:value-of select="Package"/></td></tr>
		<tr><td><b>File Path</b></td><td><xsl:value-of select="FilePath"/></td></tr>

		<tr><td colspan="2"><b>Dependencies</b></td></tr>
		<xsl:for-each select="Dependencies">
		<tr><td></td><td><xsl:value-of select="Dependency"/></td></tr>
		</xsl:for-each>

		<tr><td colspan="2"><b>Properties</b></td></tr>
		<xsl:for-each select="Properties/Property">
		<tr><td><xsl:value-of select="Name"/></td><td><xsl:value-of select="Value"/></td></tr>
		</xsl:for-each>

		</table>
		</xsl:for-each>
	</ol>
	</xsl:template>
</xsl:stylesheet>

