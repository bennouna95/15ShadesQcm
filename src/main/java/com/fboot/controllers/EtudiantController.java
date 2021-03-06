package com.fboot.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fboot.entities.Choix;
import com.fboot.entities.Etudiant;
import com.fboot.entities.Qcm;
import com.fboot.entities.Question;
import com.fboot.entities.Resultat;
import com.fboot.repositories.EtudiantRepository;
import com.fboot.repositories.QcmRepository;
import com.fboot.repositories.QuestionRepository;
import com.fboot.repositories.ResultatRepository;

@Controller
@RequestMapping("/etudiant")
public class EtudiantController {
	
	@Autowired
	private QcmRepository qcmRepo;
	
	@Autowired
	private QuestionRepository questionRepo;
	
	@Autowired
	private ResultatRepository resultatRepo;
	
	@Autowired
	private EtudiantRepository etudiantRepo;
	
	@RequestMapping("/")
    public ModelAndView welcome() {
        ModelAndView mav = new ModelAndView("accueilEtudiant");
        return mav;
    }
	@RequestMapping("/qcm/{type}")
    public ModelAndView choisirType(@PathVariable String type,HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		if(!type.equals("diagnostique")){
			if(request.getSession().getAttribute("userId")==null){
				mav = new ModelAndView("attention");
				return mav;
			}
		}
		request.getSession().setAttribute("score", 0);
		
		List<Qcm> qcms = qcmRepo.findByType(type);
		mav = new ModelAndView("typeQcm");
        mav.addObject("qcms",qcms);
        mav.addObject("type",type);
        return mav;
    }
	
	@RequestMapping("/qcm/{idQcm}/{n}")
	 public ModelAndView afficherQuestion(HttpServletRequest request,@PathVariable int idQcm,@PathVariable int n) {
		ModelAndView mav = new ModelAndView();
		Question question;
		if(questionRepo.findQuestions(idQcm, n).size()>0){
			 question = questionRepo.findQuestions(idQcm, n).get(0);
			 mav = new ModelAndView("question");
			mav.addObject("question",question);
			
		}
		else{
			Resultat resultat = new Resultat();
			resultat.setEtudiant(etudiantRepo.findOne(2));
			resultat.setQcm(qcmRepo.findOne(idQcm));
			if(request.getSession().getAttribute("score")==null) request.getSession().setAttribute("score","12");
			resultat.setNote((int) request.getSession().getAttribute("score"));
			resultatRepo.save(resultat);
			 mav = new ModelAndView("finQcm");
		}
		
		 return mav;
	}
	
	@RequestMapping(value = "/qcm/{idQcm}/{n}",method = RequestMethod.POST)
	public ModelAndView afficherReponse(HttpServletRequest request,@PathVariable int idQcm,@PathVariable int n,@RequestParam(value = "choix") String choix) {
		ModelAndView mav = new ModelAndView();
		List<Choix> listes;
		Choix correcte=null;
		String s;
		if(questionRepo.findQuestions(idQcm, n).size()>0){
			 listes = questionRepo.findQuestions(idQcm, n).get(0).getChoix();
			 for (Choix choi : listes) {
				if(choi.isCorrecte()) {correcte = choi;break;}
			}
			 mav = new ModelAndView("reponse");
		}
		
		if(correcte.getValeur().equals(choix)) {s="Vous avez raison";int score=(int)request.getSession().getAttribute("score");score=score+5;request.getSession().setAttribute("score", score);}
		else s = "Mauvaise réponse";
		mav.addObject("correcte",correcte);
		mav.addObject("phrase",s);
		return mav;
	}
	
	@RequestMapping(value = "/resultat")
	 public ModelAndView resultat(HttpServletRequest request) {
		List<Resultat> resultats = resultatRepo.findByEtudiantID((int)request.getSession().getAttribute("userId"));
		ModelAndView mav = new ModelAndView("resultat");
		mav.addObject("resultats", resultats);
		return mav;
	}
	
}
