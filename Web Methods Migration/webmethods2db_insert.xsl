<xsl:stylesheet version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/">
		<Component name="Order Insert" folder="import" description="Random Important Words">
			<databaseProfile>
				<dbwrite>
					<dynamicinsert name="Statement" tableName="OrderHeader">
						<fields name="Fields">
			  				<field name="BILL_TO_ADDR1" dataType="character" mandatory="false" enforceUnique="false"/>
					<xsl:for-each select="Values/record[@name='svc_sig']/record[@name='sig_in']/array/record/array/record">
						<xsl:element name="field">
							<xsl:attribute name="name">
								<xsl:value-of select="value[@name='field_name']"/>
							</xsl:attribute>
							<xsl:attribute name="datatype">
								<xsl:choose>
								<xsl:when test="value[@name='field_type'] = 'string'">character</xsl:when>
								<xsl:otherwise>
								</xsl:otherwise>
							</xsl:choose>
							</xsl:attribute>
						</xsl:element>
					</xsl:for-each>
				</fields>
			</dynamicinsert>
		</dbwrite>
		</databaseProfile>
		</Component>
	</xsl:template>
</xsl:stylesheet>

