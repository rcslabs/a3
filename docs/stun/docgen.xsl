<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="html">

    <xsl:output method="xml" indent="yes"
        doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" 
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>
 
    <!--XHTML document outline--> 
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <title>test1</title>
				<link href="http://twitter.github.com/bootstrap/assets/css/bootstrap.css" rel="stylesheet"/>
				<link href="http://twitter.github.com/bootstrap/assets/css/bootstrap-responsive.css" rel="stylesheet"/>
				<script src="http://code.jquery.com/jquery-latest.js"></script>
				<script src="http://twitter.github.com/bootstrap/assets/js/bootstrap.min.js"></script>
                <style type="text/css">
					body        { padding:20px; }
                    h1          { padding: 10px; padding-width: 100%; background-color: silver; font-size: 24px; }
					h2          { font-size:20px; }
					table       { width:auto !important; }
					td          { font-family: Consolas, monospace ;}
                </style>
            </head>
            <body>
                <xsl:apply-templates />
            </body>
        </html>
    </xsl:template>

    <xsl:template match="messages">
		<h1>Сообщения</h1>
		<xsl:apply-templates select="./message"/>
	</xsl:template>

    <!--Table headers and outline-->
    <xsl:template match="message">
        <h2><xsl:value-of select="./param[@name=&quot;type&quot;]/@value"/></h2>
        <p><xsl:value-of select="description"/></p>
        <table class="table table-striped table-hover">
            <tr><th>Имя</th><th>Тип</th><th>Значение</th><th>Обязательно</th><th>Описание</th></tr>
            <xsl:apply-templates select="param"/>
        </table>
        <p>
			<xsl:copy-of select="comments"/>
		</p>
    </xsl:template>
 
    <!--Table row and first two columns-->
    <xsl:template match="param">
        <!--Create variable for 'url', as it's used twice-->
        <xsl:variable name="url" select=
            "normalize-space(concat('http://', normalize-space(node()), '.', local-name(..)))"/>
        <tr>
            <td><xsl:value-of select="@name"/></td>
            <td><xsl:value-of select="@type"/></td>
            <td><xsl:value-of select="@value"/></td>
			<td style="text-align:center">
				<xsl:choose>
					<xsl:when test="@required = &quot;true&quot;">*</xsl:when>
					<xsl:otherwise>  </xsl:otherwise>
				</xsl:choose>
			</td>
            <td><xsl:value-of select="@description"/></td>
        </tr>
    </xsl:template>
 
 
</xsl:stylesheet>