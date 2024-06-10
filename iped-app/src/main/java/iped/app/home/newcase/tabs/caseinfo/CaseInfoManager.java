package iped.app.home.newcase.tabs.caseinfo;/*
 * @created 06/12/2022
 * @project IPED
 * @author Thiago S. Figueiredo
 */

import iped.app.home.newcase.model.Evidence;
import iped.app.ui.Messages;
import iped.engine.data.ReportInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CaseInfoManager {

    public void validateCasePath(Path casePath) throws CaseException {
        if( casePath == null)
            throw new CaseException(Messages.get("Home.OpenCase.InavlidCasePath"));
        if( ! Paths.get(casePath.toString(), "IPED-SearchApp.exe").toFile().exists() )
            throw new CaseException(Messages.get("Home.OpenCase.NoSearchApp"));
        if( ! Paths.get(casePath.toString(), "iped", "data", "processing_finished").toFile().exists() )
            throw new CaseException(Messages.get("Home.OpenCase.CaseNotFinished"));
    }

    public void castEvidenceListToReportInfo(ReportInfo reportInfo, ArrayList<Evidence> evidenceList){
        if( (evidenceList == null || evidenceList.isEmpty()) || (reportInfo == null) )
            return;
        for( Evidence currentEvidence : evidenceList ){
            String materialDescription = currentEvidence.getMaterial() != null ? currentEvidence.getMaterial() : currentEvidence.getAlias();
            if(materialDescription == null || materialDescription.isEmpty())
                materialDescription = (currentEvidence.getFileName() == null || currentEvidence.getFileName().isEmpty())? "no information.." : currentEvidence.getFileName();
            reportInfo.evidences.add(reportInfo.getEvidenceDescInstance(String.valueOf(evidenceList.indexOf(currentEvidence)) , materialDescription));
        }
    }

}