loadModule("/System/Platform");
loadModule("/System/Resources");
loadModule("/System/Git");

repoBranch = "master";
reposBaseLocation = getWorkspace().getLocation().append("xentis");
reposUtil = org.eclipse.egit.ui.Activator.getDefault().getRepositoryUtil();

if(!reposBaseLocation.toFile().exists()) {
	print("Cloning git@gitserver:xentis/project"+" ...")
	clone("git@gitserver:xentis/project", reposBaseLocation, "git", "git", repoBranch);
}
reposUtil.addConfiguredRepository(reposBaseLocation.append(".git").toFile());

sourceRepos = ["xnife", "xc_pltf", "xc_mo", "xc_fo", "xc_cc", "xc_one", "xc_bld", "xc_tools", "JavAMISXC", "xentisjava", "JavAMIS", "xrs"]

for each (repo in sourceRepos) {
	reposLocation = reposBaseLocation.append(repo);

	if(!reposLocation.toFile().exists()) {
		print("Cloning git@gitserver:xentis/"+repo+" ...")
		clone("git@gitserver:xentis/"+repo, reposLocation, "git", "git", repoBranch);	
	}
	reposUtil.addConfiguredRepository(reposLocation.append(".git").toFile());
}
