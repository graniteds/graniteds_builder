<%--
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.

  @author Franck WOLFF
--%><%
	Set javaImports = new TreeSet();

	for (jImport in jClass.imports) {
		if (jImport.hasImportPackage() && jImport.importPackageName != "java.lang" && jImport.importPackageName != jClass.clientType.packageName)
			javaImports.add(jImport.importQualifiedName);
	}

%>/**
 * Generated by Gfx v${gVersion} (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR.
 */

package ${jClass.clientType.packageName};<%

    ///////////////////////////////////////////////////////////////////////////
    // Write Import Statements.

    if (javaImports.size() > 0) {%>
<%
    }
    for (javaImport in javaImports) {%>
import ${javaImport};<%
    }

    ///////////////////////////////////////////////////////////////////////////
    // Write Interface Declaration.%>

public interface ${jClass.clientType.name}<%

    if (jClass.hasSuperInterfaces()) {
        %> extends <%
        boolean first = true;
        for (jInterface in jClass.superInterfaces) {
            if (first) {
                first = false;
            } else {
                %>, <%
            }
            %>${jInterface.clientType.name}<%
        }
    }

    %> {<%

    ///////////////////////////////////////////////////////////////////////////
    // Write Public Getter/Setter.

    for (jProperty in jClass.properties) {

        if (jProperty.readable || jProperty.writable) {%>
<%
            if (jProperty.writable) {%>
    void set${jProperty.capitalizedName}(${jProperty.clientType.name} value);<%
            }
            if (jProperty.readable) {%>
    ${jProperty.clientType.name} get${jProperty.capitalizedName}();<%
            }
        }
    }%>

}