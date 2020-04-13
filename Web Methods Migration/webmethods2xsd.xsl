<xsl:stylesheet version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/">
		<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
		<xsd:element name="record" type="record"/>
			<xsd:complexType name="record">
				<xsd:sequence>
					<xsl:for-each select="FLOW/MAP/%SOURCE_TARGET%/Values/record/array/record">
						<xsl:element name="xsd:element">
							<xsl:attribute name="name">
								<xsl:value-of select="value[@name='field_name']"/>
							</xsl:attribute>
							<xsl:attribute name="type">xs:<xsl:value-of select="value[@name='field_type']"/>
							</xsl:attribute>
							<xsl:choose>
								<xsl:when test="price > 10">
									<xsl:value-of select="artist"/>
								</xsl:when>
								<xsl:otherwise>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:for-each>
				</xsd:sequence>
			</xsd:complexType>
		</xsd:schema>
	</xsl:template>
</xsl:stylesheet>

