package SilkMigration;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> fOutput = new ArrayList<>();
        fOutput.add("Line,End,MatchedToType,Data");

        if (args.length != 2) {
            return;
        }


        Path filePath = Path.of(args[0]);
        String theName = filePath.getFileName().toString();
        Path outPath = Path.of(args[1] + "\\" + theName.replaceFirst("[.][^.]+$", "") + ".txt");

        String theCode;
        try {
            theCode = Files.readString(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CompilationUnit compilationUnit = StaticJavaParser.parse(theCode);

        List<ImportDeclaration> lImports = compilationUnit.findAll(ImportDeclaration.class).stream().toList();
        for (ImportDeclaration id : lImports) {
            String line = getLine(String.valueOf(id.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(id.getRange().map(range -> range.end.line)));
            String sImport = id.getNameAsString();
            fOutput.add(String.format("%s,%s,IMPORT,%s", line, endLine, sImport));
            System.out.println(line + "," + sImport);
        }

        List<SingleMemberAnnotationExpr> lAnnotator = compilationUnit.findAll(SingleMemberAnnotationExpr.class).stream().toList();
        for (SingleMemberAnnotationExpr annotator : lAnnotator) {
            String line = getLine(String.valueOf(annotator.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(annotator.getRange().map(range -> range.end.line)));
           // var theFunction = annotator.getParentNode().stream().toList();
            fOutput.add(String.format("%s,%s,ANNOTATION,%s", line, endLine, annotator));


        }

        List<InitializerDeclaration> lInitaliser = compilationUnit.findAll(InitializerDeclaration.class).stream().toList();
        for (InitializerDeclaration id : lInitaliser) {
            String line = getLine(String.valueOf(id.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(id.getRange().map(range -> range.end.line)));
            System.out.println(line + "," + id);
            fOutput.add(String.format("%s,%s,INITIALISER,%s", line, endLine, id));
        }

        List<ClassOrInterfaceDeclaration> lClassOrInterface = compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().toList();
        for (ClassOrInterfaceDeclaration id : lClassOrInterface) {
            String line = getLine(String.valueOf(id.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(id.getRange().map(range -> range.end.line)));
            String sClass = id.getNameAsString();
            String sType = "";
            String sExtends = "";
            var modifiers = id.getModifiers();
            var extTypes = id.getExtendedTypes();
            for (Node md : modifiers) {
                sType += md.toString() + " ";
                System.out.println(line + "," + md);
            }
            for (Node et : extTypes) {
                sExtends += et.toString() + " ";
                System.out.println(line + "," + et);
            }

            fOutput.add(String.format("%s,%s,CLASS,%s:%s:%s", line, endLine, sClass, sType.trim(), sExtends.trim()));
            System.out.println(line + "," + sClass);
        }


        List<MethodDeclaration> lMethods = compilationUnit.findAll(MethodDeclaration.class).stream().toList();
        for (MethodDeclaration md : lMethods) {
            String line = getLine(String.valueOf(md.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(md.getRange().map(range -> range.end.line)));
            String methodName = md.getNameAsString();
            List<AnnotationExpr> anos = md.getAnnotations().stream().toList();

            for (AnnotationExpr ano : anos) {
                String anoName = ano.getName().toString();
                String anoLine = getLine(String.valueOf(ano.getRange().map(range -> range.begin.line)));
                String anoEndLine = getLine(String.valueOf(ano.getRange().map(range -> range.end.line)));
                if (ano.getChildNodes().size() > 1) {
                    if (anoName.equals("Test")){
                        continue;
                    }
                    if (anoName.equals("Keyword") || anoName.equals("KeywordGroup")) {
                         fOutput.add(String.format("%s,%s,KEYWORD_METHOD,%s:%s", anoLine, anoEndLine, ano.getChildNodes().get(1).toString(),methodName));
                    } else {
                        fOutput.add(String.format("%s,%s,METHOD,%s:%s", anoLine, anoEndLine, anoName, ano.getChildNodes().get(1).toString()));
                    }

                } else {
                    if (anoName.equals("Test")){
                        continue;
                    }
                    if (anoName.equals("Keyword") || anoName.equals("KeywordGroup")) {
                        fOutput.add(String.format("%s,%s,KEYWORD_METHOD,%s:%s", anoLine, anoEndLine, ano.getChildNodes().get(1).toString(),methodName));
                    } else {
                        fOutput.add(String.format("%s,%s,METHOD,%s:%s", anoLine, anoEndLine, ano.getName(), ano.getChildNodes().get(0).toString()));
                    }

                }
            }
        }

        List<ConstructorDeclaration> lConstructor = compilationUnit.findAll(ConstructorDeclaration.class).stream().toList();
        for (ConstructorDeclaration id : lConstructor) {
            String line = getLine(String.valueOf(id.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(id.getRange().map(range -> range.end.line)));
            System.out.println(line + "," + id);
        }

        List<CompactConstructorDeclaration> lCompactConstructor = compilationUnit.findAll(CompactConstructorDeclaration.class).stream().toList();
        for (CompactConstructorDeclaration id : lCompactConstructor) {
            String line = getLine(String.valueOf(id.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(id.getRange().map(range -> range.end.line)));
            System.out.println(line + "," + id);
        }

        /*
        This is covered by FieldDeclaration
        List<VariableDeclarator> lVariable = compilationUnit.findAll(VariableDeclarator.class).stream().toList();
        */
        List<FieldDeclaration> lFields = compilationUnit.findAll(FieldDeclaration.class).stream().toList();
        for (FieldDeclaration fd : lFields) {
            String line = getLine(String.valueOf(fd.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(fd.getRange().map(range -> range.end.line)));
            //List<Modifier> mods = fd.getModifiers().stream().toList();
            List<VariableDeclarator> vars = fd.getVariables().stream().toList();
            for (VariableDeclarator aVar : vars) {
                System.out.println(line + "," + aVar.getName() + " - " + aVar.getTypeAsString());
                fOutput.add(String.format("%s,%s,FIELD,%s:%s:%s", line, endLine, aVar.getName(), aVar.getTypeAsString(),aVar.getInitializer()));
            }

        }

        List<ModuleDeclaration> lModule = compilationUnit.findAll(ModuleDeclaration.class).stream().toList();
        for (ModuleDeclaration id : lModule) {
            String line = getLine(String.valueOf(id.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(id.getRange().map(range -> range.end.line)));
            System.out.println(line + "," + id);
        }

        List<VariableDeclarationExpr> lVariables = compilationUnit.findAll(VariableDeclarationExpr.class);
        for (VariableDeclarationExpr v : lVariables) {
            String line = getLine(String.valueOf(v.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(v.getRange().map(range -> range.end.line)));
            var Variable = v.getVariable(0);
            String vType = Variable.getType().asString();
            String vName = Variable.getNameAsString();
            fOutput.add(String.format("%s,%s,VARIABLE,%s:%s", line, endLine, vName, vType));
        }

        List<ExpressionStmt> estmt = compilationUnit.findAll(ExpressionStmt.class).stream().toList();
        for (ExpressionStmt exp : estmt) {
            String line = getLine(String.valueOf(exp.getRange().map(range -> range.begin.line)));
//            if(line.equals("19241")){
//                System.out.println(line + "," + line);
//            }
            String endLine = getLine(String.valueOf(exp.getRange().map(range -> range.end.line)));
            List<Node> parts = exp.getExpression().getChildNodes();
            if (parts.size() > 1) {
                for (Node p : parts) {
                    if(p.toString().contains("\n")){
                        List<String> ps = Arrays.stream(p.toString().split("\n" )).toList();
                        for (String thisP:ps) {
                            fOutput.add(String.format("%s,%s,EXPRESSION,%s", line, endLine, thisP.replace("\r","")));
                        }
                    }
                    else {
                        fOutput.add(String.format("%s,%s,EXPRESSION,%s", line, endLine, p));
                    }
                }
            }
        }

        List<TypeDeclaration> lType = compilationUnit.findAll(TypeDeclaration.class).stream().toList();
        for (TypeDeclaration id : lType) {
            String line = getLine(String.valueOf(id.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(id.getRange().map(range -> range.end.line)));
            String sType = "";
            String sClass = id.getNameAsString();
            var modifiers = id.getModifiers();
            for (Object md : modifiers) {
                sType += md.toString() + " ";
                System.out.println(line + "," + md);
            }
            fOutput.add(String.format("%s,%s,DECLERATION,%s:%s", line, endLine, sClass, sType.trim()));
            System.out.println(line + "," + id.getNameAsString());
        }


        List<LocalClassDeclarationStmt> lLocalClass = compilationUnit.findAll(LocalClassDeclarationStmt.class).stream().toList();
        for (LocalClassDeclarationStmt id : lLocalClass) {
            String line = getLine(String.valueOf(id.getRange().map(range -> range.begin.line)));
            String endLine = getLine(String.valueOf(id.getRange().map(range -> range.end.line)));
            System.out.println(line + "," + id);
        }


        try {
            Files.write(outPath, fOutput, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static String getLine(String l) {

        return l.replace("Optional[", "").replace("]", "");
    }

}